import axios from "axios";
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import { ElMessage } from "element-plus";

// 后端统一响应结构
interface ApiResponse {
  code: number;
  message: string;
  data: unknown;
}

class RequestService {
  private instance: AxiosInstance;

  constructor() {
    // 创建 axios 实例
    this.instance = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
      timeout: 30000,
    });

    // 请求拦截器：自动携带 token
    this.instance.interceptors.request.use((config) => {
      const token = localStorage.getItem("token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    // 响应拦截器：统一处理业务错误，返回完整 response
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => {
        // blob 类型（文件下载）直接返回
        if (response.config.responseType === "blob") {
          return response;
        }
        const res: ApiResponse = response.data;
        if (res.code !== 200) {
          ElMessage.error(res.message || "请求失败");
          return Promise.reject(new Error(res.message || "请求失败"));
        }
        return response;
      },
      (error) => {
        ElMessage.error(error.message || "网络错误");
        return Promise.reject(error);
      },
    );
  }

  /** GET 请求 */
  get<T = unknown>(
    url: string,
    params?: object,
    config?: AxiosRequestConfig,
  ): Promise<T> {
    return this.instance
      .get(url, { params, ...config })
      .then((res) => res.data.data);
  }

  /** POST 请求 */
  post<T = unknown>(
    url: string,
    data?: object | string,
    config?: AxiosRequestConfig,
  ): Promise<T> {
    return this.instance.post(url, data, config).then((res) => res.data.data);
  }

  /** PUT 请求 */
  put<T = unknown>(
    url: string,
    data?: object,
    config?: AxiosRequestConfig,
  ): Promise<T> {
    return this.instance.put(url, data, config).then((res) => res.data.data);
  }

  /** DELETE 请求 */
  del<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete(url, config).then((res) => res.data.data);
  }

  /** 文件导入（上传） */
  upload<T = unknown>(
    url: string,
    file: File,
    onProgress?: (percent: number) => void,
  ): Promise<T> {
    const formData = new FormData();
    formData.append("file", file);
    return this.instance
      .post(url, formData, {
        headers: { "Content-Type": "multipart/form-data" },
        onUploadProgress: (e) => {
          if (e.total && onProgress) {
            onProgress(Math.round((e.loaded / e.total) * 100));
          }
        },
      })
      .then((res) => res.data.data);
  }

  /** 文件下载（blob 方式） */
  download(url: string, params?: object, filename?: string): Promise<void> {
    return this.instance
      .get(url, { params, responseType: "blob" })
      .then((res) => {
        const blob = new Blob([res.data]);
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = filename || "export.xlsx";
        link.click();
        URL.revokeObjectURL(link.href);
      });
  }
}

const request = new RequestService();

export default request;
