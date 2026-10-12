<template>
  <h3>列表渲染</h3>
  <div v-for="(item, index) in list" :key="item.id">
    <p>{{ index }} --> {{ item.name }}</p>
  </div>
  <div v-for="item in avatar" :key="item.id">
    <img :src="item.avatar" alt="头像" />
  </div>

  <div>
    <p v-for="(value, key, index) of userInfo">
      {{ index }} --> {{ key }} : {{ value }}
    </p>
  </div>
</template>

<script>
export default {
  data() {
    return {
      list: [
        { id: 1, name: "张三" },
        { id: 2, name: "李四" },
        { id: 3, name: "王五" },
      ],
      avatar: [
        {
          id: 1,
          avatar:
            "https://img0.baidu.com/it/u=1871689052,1550910223&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
        },
        {
          id: 2,
          avatar:
            "https://img1.baidu.com/it/u=1871689052,1550910223&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
        },
        {
          id: 3,
          avatar:
            "https://img2.baidu.com/it/u=1871689052,1550910223&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500",
        },
      ],
      userInfo: {
        name: "张三",
        age: 18,
        sex: "男",
      },
    };
  },
};
</script>
