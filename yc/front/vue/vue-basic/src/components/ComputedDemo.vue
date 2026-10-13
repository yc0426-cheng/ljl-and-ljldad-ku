<template>
  <h3>计算属性</h3>
  <div>
    <p>{{ userInfo.name }}</p>
    <p>{{ userInfoList }}</p>
  </div>
</template>

<script>
export default {
  data() {
    return {
      userInfo: {
        name: "chuChu",
        list: ["1", "3", "4"],
      },
    };
  },
  // 计算属性 可以使用函数代替，但是计算属性会缓存，只有更改时才会改变，而方法每次调用都会改变
  computed: {
    userInfoList() {
      return this.userInfo.list.length > 0 ? "yes" : "no";
    },
  },
};
</script>
