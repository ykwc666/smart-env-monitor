import OSS from 'ali-oss'

/**
 * 阿里云 OSS 客户端。密钥请用环境变量注入，不要写进仓库。
 * Vite: VITE_OSS_ACCESS_KEY_ID / VITE_OSS_ACCESS_KEY_SECRET / VITE_OSS_BUCKET / VITE_OSS_REGION
 */
export function client() {
  const accessKeyId = import.meta.env.VITE_OSS_ACCESS_KEY_ID
  const accessKeySecret = import.meta.env.VITE_OSS_ACCESS_KEY_SECRET
  const bucket = import.meta.env.VITE_OSS_BUCKET || 'your-bucket'
  const region = import.meta.env.VITE_OSS_REGION || 'oss-cn-beijing'

  if (!accessKeyId || !accessKeySecret) {
    throw new Error('Missing VITE_OSS_ACCESS_KEY_ID / VITE_OSS_ACCESS_KEY_SECRET')
  }

  return new OSS({
    region,
    accessKeyId,
    accessKeySecret,
    bucket,
  })
}
