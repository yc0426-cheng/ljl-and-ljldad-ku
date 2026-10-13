<template>
  <h3>Key的使用</h3>
  <div v-for="(item, index) in list" :key="item.id">
    <p>{{ index }} --> {{ item.name }}</p>
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
    };
  },
};
</script>
