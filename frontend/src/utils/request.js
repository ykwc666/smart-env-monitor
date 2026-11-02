import axios from 'axios'
const baseURL = 'http://127.0.0.1:5000'

const instance = axios.create({
  // TODO 1. 基础地址，超时时间
  baseURL,
  timeout: 10000,
})
export default instance
export { baseURL }
