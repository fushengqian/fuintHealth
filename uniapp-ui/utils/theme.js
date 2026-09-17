import * as themeApi from '@/api/theme'
import { getMerchantScope, isMerchantReady } from './merchant'

// 不使用任何兜底主题色：后台未配置主题（或主题尚未拉取到）时不注入主色，
// 页面保持自身的默认样式，避免出现「假的默认主题」。主题接口返回后立即生效。
function emptyTheme() {
  return {
    themeId: '',
    themeName: '',
    colors: {}
  }
}

// 颜色键与 CSS 变量名的对应关系
const THEME_VARS = {
  primary: '--theme-primary',
  secondary: '--theme-secondary',
  text: '--theme-text',
  bg: '--theme-bg',
  price: '--theme-price'
}

// 主题缓存有效期:1 小时
// (App 启动时已通过 loadTheme(true) 强制拉取最新主题并写入缓存,
// 因此页面 onShow 期间只需在缓存超时后兜底刷新, 避免每个页面反复请求导致换色闪烁)
const CACHE_DURATION = 60 * 60 * 1000

let loadingPromise = null

/**
 * 读取缓存的主题配置
 */
export function getTheme() {
  const theme = uni.getStorageSync('theme')
  return theme && theme.colors ? theme : emptyTheme()
}

/**
 * 缓存主题配置
 */
export function setTheme(theme, scope) {
  uni.setStorageSync('theme', { ...theme, _scope: scope || getMerchantScope() })
  uni.setStorageSync('theme_time', Date.now())
}

/**
 * 生成页面 CSS 变量样式字符串,用于页面根节点 :style 绑定
 *
 * 注意必须返回字符串而非对象：uni-app 编译到微信小程序时,
 * :style="obj" 会被序列化为 style="{{(obj)}}",对象会变成 [object Object],
 * CSS 变量在 page 内彻底失效。字符串形式在 H5 与小程序端都会被作为 inline style 正确解析。
 *
 * 只为后台实际下发的颜色生成变量，未配置的颜色不生成，交由页面自身默认样式决定。
 */
export function buildThemeVars(theme) {
  const t = theme || getTheme()
  const colors = (t && t.colors) || {}
  const parts = []
  Object.keys(THEME_VARS).forEach((key) => {
    if (colors[key]) {
      parts.push(`${THEME_VARS[key]}: ${colors[key]}`)
    }
  })
  // 同时同步 SCSS 编译后对应的 CSS 变量，让 $fuint-theme 的 100+ 处引用也跟随主题
  if (colors.primary) {
    parts.push(`--fuint-theme: ${colors.primary}`)
  }
  return parts.join('; ')
}

/**
 * 读取当前主题的 primary 色（用于组件如 tabbar 选中色、导航栏等无 CSS 变量场景）
 * 未配置主题时返回空字符串，调用方据此自行决定是否设置颜色
 */
export function getThemePrimary() {
  const t = getTheme()
  return (t && t.colors && t.colors.primary) || ''
}

/**
 * 判断颜色是否为浅色(用于导航栏前景文字黑/白选择)
 * @param {string} color 如 '#ffffff'
 */
export function isLightColor(color) {
  const hex = String(color || '').trim().replace('#', '')
  if (!/^[0-9a-fA-F]{6}$/.test(hex)) return false
  const r = parseInt(hex.substr(0, 2), 16)
  const g = parseInt(hex.substr(2, 2), 16)
  const b = parseInt(hex.substr(4, 2), 16)
  // 感知亮度(0~255), 大于 160 视为浅色, 前景用深色文字
  return 0.299 * r + 0.587 * g + 0.114 * b > 160
}

/**
 * 注入主题 CSS 变量(全站生效, 页面无需再在根节点绑定 themeVars)
 * - H5: 写入 document.documentElement, 所有页面/组件都能读取
 * - 微信小程序: 通过 wx.setPageStyle 把变量写到当前页面 page 节点的 cssText,
 *   页面每次 onShow 都会重新写入; 基础库过低时静默忽略(fail 回调),
 *   此时仍可由页面根节点 :style="themeVars" 兜底
 *
 * 未配置的颜色会被移除，避免沿用上一个商户/上一个页面的主题变量
 */
export function applyGlobalTheme(theme) {
  const t = theme || getTheme()
  const colors = (t && t.colors) || {}
  // #ifdef H5
  const style = document.documentElement.style
  Object.keys(THEME_VARS).forEach((key) => {
    if (colors[key]) {
      style.setProperty(THEME_VARS[key], colors[key])
    } else {
      style.removeProperty(THEME_VARS[key])
    }
  })
  if (colors.primary) {
    style.setProperty('--fuint-theme', colors.primary)
  } else {
    style.removeProperty('--fuint-theme')
  }
  // #endif
  // #ifdef MP-WEIXIN
  try {
    if (typeof wx !== 'undefined' && typeof wx.setPageStyle === 'function') {
      // 普通 CSS 属性写 style 字段, CSS 变量这类特殊样式需要写在 cssText 里
      wx.setPageStyle({
        style: { cssText: buildThemeVars(t) },
        fail() {}
      })
    }
  } catch (e) {}
  // #endif
}

/**
 * 加载主题配置(带缓存,force 为 true 时强制刷新)
 */
export function loadTheme(force) {
  const scope = getMerchantScope()
  const cached = uni.getStorageSync('theme')
  const cachedTheme = cached && cached.colors ? cached : emptyTheme()
  const scopeMatched = !!(cached && cached._scope === scope)
  const time = uni.getStorageSync('theme_time')
  const inCacheTime = !!(time && Date.now() - time < CACHE_DURATION)

  // 切换店铺后商户号尚未就绪(systemConfig 未返回)时不请求,
  // 否则请求头带的仍是上一个商户的商户号, 会拉到错误商户的主题
  if (!isMerchantReady()) {
    applyGlobalTheme(cachedTheme)
    return Promise.resolve(cachedTheme)
  }

  // 缓存命中条件:未强制刷新 + 商户/店铺作用域一致 + 未超过缓存有效期
  if (!force && scopeMatched && inCacheTime) {
    applyGlobalTheme(cachedTheme)
    return Promise.resolve(cachedTheme)
  }

  // 防止并发重复请求:作用域一致时复用同一个请求
  if (loadingPromise) {
    if (loadingPromise.scope === scope) {
      return loadingPromise
    }
    // 作用域已变化(切换了商户/店铺), 等当前请求结束后按新作用域重新拉取
    return loadingPromise.then(() => loadTheme(force))
  }

  loadingPromise = themeApi.theme()
    .then(res => {
      const theme = res.data || {}
      if (!theme.colors) {
        theme.colors = {}
      }
      setTheme(theme, scope)
      applyGlobalTheme(theme)
      return theme
    })
    .catch(() => {
      const theme = getTheme()
      applyGlobalTheme(theme)
      return theme
    })
    .finally(() => {
      loadingPromise = null
    })
  // 记录本次请求所属作用域, 供并发调用判断是否可复用
  loadingPromise.scope = scope
  return loadingPromise
}
