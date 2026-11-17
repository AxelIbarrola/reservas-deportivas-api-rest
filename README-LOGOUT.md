# Logout pedagógico con JWT revocado

`POST /api/auth/logout` requiere `Authorization: Bearer <JWT>`.
El filtro `AuthTokenFilter` valida firma, vencimiento y ausencia del `jti` en
la tabla de revocados. Al cerrar sesión, `TokenRevocationService` persiste
`jti` y vencimiento en H2. Las peticiones posteriores con ese token reciben
**401**. Un nuevo inicio de sesión emite un JWT con otro `jti`.

El frontend también lo borra del `sessionStorage` al salir. **Solo se revoca
el token enviado**, no todos los dispositivos o sesiones simultáneas.

La demo actual escucha en **8086** y expone los endpoints `/api/auth/signup`,
`/api/auth/signin`, `/api/auth/me`, `/api/auth/logout`. `/signin` devuelve JSON
`{"token":"...","usuario":"...","rol":"USER"}`.
La guía general y las instrucciones para probar están en `../README.md`.
