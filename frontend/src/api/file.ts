import { post } from "@/utils/request"

/**
 * 上传文件到后端文件服务
 * @param file 文件
 * @param category 分类目录（如 rejudgment-evidence）
 * @returns 相对路径，存入业务字段 evidenceAttachmentUrl 等
 */
export const uploadFile = (file: File, category: string) => {
  const formData = new FormData()
  formData.append("file", file)
  return post<string>(`/files/upload?category=${encodeURIComponent(category)}`, formData, {
    headers: { "Content-Type": "multipart/form-data" }
  })
}
