import request from '@/utils/request'
import config from '@/config'

// api地址
const api = {
  info: 'merchantApi/member/info',
  list: 'merchantApi/member/list',
  save: 'merchantApi/member/save',
  exportHealthReport: 'merchantApi/member/exportHealthReport',
  saveHealthRecord: 'merchantApi/member/saveHealthRecord',
  getLatestHealthRecord: 'merchantApi/member/getLatestHealthRecord',
  getExchangeableCoupons: 'merchantApi/member/getExchangeableCoupons',
  exchangeCoupon: 'merchantApi/member/exchangeCoupon',
}

// 会员详情
export function detail(memberId, param) {
  return request.post(api.info, { memberId, ...param })
}

// 会员列表
export function list(param, option) {
  return request.post(api.list, param, option)
}

// 保存会员信息
export const save = (param, option) => {
  return request.post(api.save, param)
}

// 导出健康报告
export function exportHealthReport(memberId) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.downloadFile({
      url: config.apiUrl + api.exportHealthReport + '?memberId=' + memberId,
      header: {
        'token': token
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.tempFilePath)
        } else {
          reject(new Error('下载失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

// 保存体检数据
export function saveHealthRecord(param) {
  return request.post(api.saveHealthRecord, param)
}

// 获取最新体检数据
export function getLatestHealthRecord(memberId) {
  return request.get(api.getLatestHealthRecord, { memberId })
}

// 获取可兑换优惠券列表
export function getExchangeableCoupons(memberId) {
  return request.get(api.getExchangeableCoupons, { memberId })
}

// 积分兑换优惠券
export function exchangeCoupon(param) {
  return request.post(api.exchangeCoupon, param)
}