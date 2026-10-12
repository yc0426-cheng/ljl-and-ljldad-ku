<template>
    <div class="aside">
        <h3>Aside</h3>
        <Item />
        <Item />
        <Item />
    </div>
</template>

<script>
import Item from './Item.vue'
export default {
    components: {
        Item
    }
}
</script>

<style scoped>
.aside {
    float: right;
    width: 30%;
    height: 400px;
    border: 5px solid #999;
    box-sizing: border-box;
    border-left: 0px;
}
</style>