<template>
    <h3>Parent</h3>
    <Children :text="message" :names="names"/>
</template>

<script>
import Children from './children.vue';

export default {
    data() {
        return {
            message: "输出我是动态的",
            names: ["ien", "thc", "sch"]
        }
    },
    components: {
        Children
    }
}
</script>