const fetch = require('node-fetch');

async function test() {
    try {
        console.log("Registering user...");
        const regRes = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: "testuser99",
                email: "test99@example.com",
                password: "password123"
            })
        });
        const regData = await regRes.json();
        console.log(regData);

        console.log("Logging in...");
        const loginRes = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: "testuser99",
                password: "password123"
            })
        });
        const loginData = await loginRes.json();
        console.log("Login response:", loginData);

        const token = loginData.data?.token;
        if (!token) {
            console.log("No token received");
            return;
        }

        console.log("Creating URL with token: " + token.substring(0, 10) + "...");
        const shortenRes = await fetch('http://localhost:8080/api/shorten', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                url: "https://www.google.com"
            })
        });
        const shortenData = await shortenRes.json();
        console.log("Shorten response:", shortenData);

        console.log("Getting user URLs...");
        const getRes = await fetch('http://localhost:8080/api/user/urls', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const getData = await getRes.json();
        console.log("User URLs:", getData);

    } catch (e) {
        console.error(e);
    }
}

test();
