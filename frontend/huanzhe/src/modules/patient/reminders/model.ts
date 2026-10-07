export const beijingTime = (value: string) => new Date(value).toLocaleTimeString('zh-CN', {
  hour: '2-digit', minute: '2-digit', hourCycle: 'h23', timeZone: 'Asia/Shanghai'
})
