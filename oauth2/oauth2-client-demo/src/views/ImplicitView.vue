<template>
  <div class="implicit">
    <user-card :User="userInfo" style="width: 400px; height: 500px" />
    <button
      class="implicit__authorize-btn"
      @click="tryAuthorize(OAuth2GrantType.AUTHORIZATION_CODE)"
    >
      从第三方应用获取权限
    </button>
  </div>
</template>

<script setup>
  import Cookies from 'js-cookie'
  import UserCard from '@/components/UserCard.vue'
  import { onBeforeMount, ref } from 'vue'
  import { getAccessToken, tryAuthorize, OAuth2GrantType } from '@/api/authorize'
  import { getUserInfo } from '@/api/user'

  const access_token = ref(Cookies.get('access_token'))

  const url = new URL(window.location.href)
  const code = ref(url.searchParams.get('code'))

  const userInfo = ref({
    avatar: '',
    name: '',
    email: '',
  })

  const getAccessTokenByCode = async () => {
    if (!code.value) {
      return
    }

    const res = await getAccessToken(code.value)
    console.log('根据授权码获取到的对象: ', res)

    access_token.value = res.access_token
    Cookies.set('access_token', access_token.value, { expires: 7 })

    setUserInfo(access_token.value)
  }

  const setUserInfo = async (token) => {
    try {
      const res = await getUserInfo(token)
      console.log('获取到的用户信息: ', res)
      if (!res) {
        Cookies.remove('access_token')
        return console.error('未获取到用户信息, access_token可能已过期或无效')
      }

      userInfo.value.avatar = res.avatar
      userInfo.value.name = res.name
      userInfo.value.email = res.email
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  onBeforeMount(() => {
    if (access_token.value) {
      setUserInfo(access_token.value)
    } else {
      getAccessTokenByCode()
    }
  })
</script>

<style scoped>
  .implicit {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  .implicit__authorize-btn {
    width: 400px;
    height: 60px;
    margin-top: 20px;
    background-color: #1976d2;
    color: #fff;
    border: none;
    border-radius: 5px;
    font-size: 16px;
    cursor: pointer;
  }
</style>
