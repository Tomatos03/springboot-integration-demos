<template>
  <div class="oauth2-client">
    <div class="oauth2-client__container">
      <h1 class="oauth2-client__title">OAuth2 Client Demo</h1>
      <div class="oauth2-client__options">
        <router-link to="/authorization-code" class="oauth2-client__option">
          Authorization Code
        </router-link>
        <router-link to="/implicit" class="oauth2-client__option"> Implicit </router-link>
        <router-link to="/client-credentials" class="oauth2-client__option">
          Client Credentials
        </router-link>
        <router-link to="/resource-owner-password" class="oauth2-client__option">
          Resource Owner Password
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .oauth2-client {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .oauth2-client__title {
    font-size: 2.2rem;
    font-weight: bold;
    color: #1976d2;
    margin-bottom: 32px;
    text-align: center;
    letter-spacing: 1px;
  }

  .oauth2-client__options {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background-color: #fff;
    width: 400px;
    box-shadow: 0 4px 16px rgba(25, 118, 210, 0.15);
  }

  .oauth2-client__option {
    width: 220px;
    height: 100px;
    margin: 16px 0;
    padding: 12px 0;
    background-color: #1976d2;
    color: #fff;
    border: none;
    border-radius: 6px;
    font-size: 18px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
    text-decoration: none;
  }

  .oauth2-client__option:hover {
    background-color: #1565c0;
  }
</style>
