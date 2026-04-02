document.addEventListener("DOMContentLoaded", () => {
    const loginBtn = document.getElementById("login-btn");
    const logoutBtn = document.getElementById("logout-btn");
    const authArea = document.getElementById("auth-area");
    const userInfo = document.getElementById("user-info");
    const avatar = document.getElementById("avatar");
    const username = document.getElementById("username");

    // 登录按钮逻辑
    loginBtn.onclick = () => {
        window.location.href = "/oauth2/authorization/github";
    };

    // 登出按钮逻辑
    logoutBtn.onclick = () => {
        fetch("/logout", {method: "GET", credentials: "include"})
            .then(() => window.location.reload());
    };

    // 检查登录状态
    fetch("/api/userinfo", {credentials: "include"})
        .then(async resp => {
            if (resp.ok) {
                const data = await resp.json();
                if (data && data.username) {
                    authArea.style.display = "none";
                    userInfo.style.display = "block";
                    avatar.src = data.avatar_url || "https://github.githubassets.com/images/modules/logos_page/GitHub-Mark.png";
                    username.textContent = data.username;
                }
            } else {
                authArea.style.display = "block";
                userInfo.style.display = "none";
            }
        })
        .catch(() => {
          authArea.style.display = "block";
          userInfo.style.display = "none";
        });
});
