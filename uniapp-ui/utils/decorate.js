import { loadTheme, buildThemeVars } from '@/utils/theme'
import { loadAndApplyTabbar } from '@/utils/tabbar'

/**
 * 强制刷新当前商户/店铺的主题与底部导航
 *
 * 主题与底部导航均按「商户号 + 店铺ID」下发，切换商户/店铺后必须强制重新拉取，
 * 否则会沿用上一家的缓存配置。原先这段逻辑只写在首页，直达其它 tab 页时不会触发，
 * 这里抽成公共方法，供全局 mixin 与各 tab 页面调用。
 *
 * @param {Object} page 页面实例（Vue 页面组件）
 */
export function refreshMerchantConfig(page) {
  // 先强制拉取主题：底部导航的选中色默认取主题色，主题就绪后再拉导航才能跟随后台主题
  loadTheme(true).then(theme => {
    if (!page) {
      return
    }
    // 同步页面 CSS 变量
    page.themeVars = buildThemeVars(theme)
    // 强制拉取底部导航配置并应用到自定义 tabBar
    loadAndApplyTabbar(page, true)
    // #ifdef H5
    // H5 无微信自定义 tabBar 机制，由页面内组件渲染，需要单独刷新
    if (page.$refs && page.$refs.h5Tabbar) {
      page.$refs.h5Tabbar.refresh(true)
    }
    // #endif
    // #ifdef MP-WEIXIN
    const host = page.$scope || page
    const tabBar = typeof host.getTabBar === 'function' && host.getTabBar()
    tabBar && tabBar.syncSelected && tabBar.syncSelected()
    // #endif
  })
}
