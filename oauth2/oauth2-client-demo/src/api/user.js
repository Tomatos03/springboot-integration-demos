export const getUserInfo = async (token) => {
  if (!token) {
    return
  }

  try {
    const response = await fetch('http://localhost:8081/userinfo', {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    })
    return await response.json()
  } catch (error) {
    console.error('请求失败:', error)
  }
}
