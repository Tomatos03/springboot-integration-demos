// OAuth2 配置
export const oauth_config = {
  auth_server_url: 'http://localhost:9000', // 替换为您的授权服务器地址
  client_id: 'userinfo-client', // 替换为您的客户端ID
  redirect_uri: 'http://localhost:5173/implicit', // 替换为您的重定向URI
  scope: 'read_userinfo', // 替换为您的请求范围
  authorize_url: '/oauth2/authorize', // 授权端点
  token_url: '/oauth2/token', // 令牌端点
}

export const OAuth2GrantType = {
  AUTHORIZATION_CODE: 'authorization_code',
  PASSWORD: 'password',
  CLIENT_CREDENTIALS: 'client_credentials',
}

export const tryAuthorize = (type) => {
  //authorization-grant-types
  const authUrl = getAuthUrl(type)
  window.location.href = authUrl
}

export const getAccessToken = async (code) => {
  // 需要在 Authorization header 中添加 Basic Auth
  const basicAuth = btoa(`${oauth_config.client_id}:secret`)
  const response = await fetch(`${oauth_config.auth_server_url}${oauth_config.token_url}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      Authorization: `Basic ${basicAuth}`,
    },
    body: new URLSearchParams({
      grant_type: 'authorization_code',
      code: code,
      redirect_uri: oauth_config.redirect_uri,
    }),
  })

  if (!response.ok) {
    throw new Error('获取 access token 失败')
  }

  return response.json()
}

const getAuthUrl = (type) => {
  const params = new URLSearchParams({
    response_type: 'code',
    client_id: oauth_config.client_id,
    redirect_uri: oauth_config.redirect_uri,
    scope: oauth_config.scope,
    state: Math.random().toString(36).substring(7), // 简单的state参数
  })

  return `${oauth_config.auth_server_url}${oauth_config.authorize_url}?${params.toString()}`
}
