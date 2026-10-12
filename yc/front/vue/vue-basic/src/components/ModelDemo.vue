<template>
  <p>表单输入绑定</p>
  <input v-model.lazy="message" />
  <p>{{ message }}</p>

  <input type="checkbox" id="checkbox" v-model="checked" />
  <label for="checked">{{ checked }}</label>
</template>

<script>
export default {
  data() {
    return {
      message: "",
      checked: false,
    };
  },
};
</script>
