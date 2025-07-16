<template class="client-credentials">
  <user-card :User="userInfo" style="width: 400px; height: 500px" />
  <button class="authorization-code__authorize-btn" @click="tryAuthorize">
    从第三方应用获取权限
  </button>
</template>

<script setup>
  import { defineProps, computed } from 'vue'
  import UserCard from '@/components/UserCard.vue'
  const props = defineProps({
    User: {
      type: Object,
      required: true,
    },
  })
  const user = computed(() => props.User)
</script>

<style scoped></style>
