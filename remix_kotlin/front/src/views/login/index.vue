<template>
  <div class="login">
    <el-card class="login__card">
      <h2 class="login__title">系统登录</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input
            v-model="form.account"
            placeholder="账号"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login__btn"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { AuthApi } from "@/api/auth/auth";
import type { LoginDTO } from "@/types/auth";

const router = useRouter();
const loading = ref(false);

const form = reactive<LoginDTO>({
  account: "",
  password: "",
});

async function handleLogin(): Promise<void> {
  if (!form.account || !form.password) {
    ElMessage.warning("请输入账号和密码");
    return;
  }
  loading.value = true;
  try {
    const token = await AuthApi.login(form);
    localStorage.setItem("token", token);
    ElMessage.success("登录成功");
    router.push("/home");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login__card {
  width: 380px;
}
.login__title {
  text-align: center;
  margin-bottom: 24px;
}
.login__btn {
  width: 100%;
}
</style>
