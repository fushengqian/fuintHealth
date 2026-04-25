import request from '@/utils/request'
import config from '@/config'

// api地址
const api = {
  info: 'merchantApi/member/info',
  list: 'merchantApi/member/list',
  save: 'merchantApi/member/save',
  exportHealthReport: 'merchantApi/member/exportHealthReport',
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