export interface LoginUserInfo {
  userId: string;
  account: string;
  name: string;
  phone?: string;
  email?: string;
  nickname?: string;
  signature?: string;
  avatar?: string;
  token?: string;
}

export interface BaseEntity {
  createUser: string;
  createTime: string;
  updateUser: string;
  updateTime: string;
}
