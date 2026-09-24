# TECH_STACK.md

## 1. Document purpose

This file is the authoritative technology stack document for the project.

Rules:

1. `Standard document/TECH_STACK.md` is the source of truth for future development.
2. Existing manifests can be inspected for current implementation state, but standard versions must come from this file.
3. Version ranges such as `^`, `~`, `latest`, `mysql:8`, `redis:6-alpine`, and `nginx:alpine` are not acceptable as a standard definition.
4. When code and this document conflict:
   - Existing legacy code is treated as `Legacy Frozen`
   - New development must follow `MVP Standard`

## 2. Stack policy

The workspace is split into two technology zones:

1. `Legacy Frozen`
   - Existing `backend`
   - Existing old runtime images and old admin ecosystems
   - Kept for reference and compatibility
2. `MVP Standard`
   - New product documentation baseline
   - Future product and backend-v2 implementation baseline
   - Exact versions only

## 3. API baseline

### 3.1 Contract identity

- Contract baseline: `Yaoshian MVP API v1`
- Response envelope: `CommonResult<T>`

Exact response shape:

```json
{
  "code": 200,
  "msg": "OK",
  "data": {}
}
```

### 3.2 Route baseline

- Patient and family account API prefix: `/app-api/**`
- Pharmacist and doctor API prefix: `/admin-api/**`

### 3.3 Transport and realtime baseline

- Primary API style: JSON over HTTP/HTTPS
- Realtime notification for Web: WebSocket
- OCR input transport: multipart file upload
- Help-order speech-to-text provider: XFYUN voice dictation streaming WebAPI, proxied by `backend-v2`; credentials are environment variables only

### 3.4 Port baseline

- Legacy admin backend: `48080`
- Legacy patient backend: `48081`
- Patient app local dev: `3001`
- Doctor web local dev: `3002`
- Pharmacist web local dev: `3003`

## 4. Toolchain

### 4.1 Current local toolchain detected in this workspace

These are the exact versions detected on the current machine:

| Tool | Exact version |
|---|---|
| Node.js (current shell) | `22.22.3` |
| npm | `11.6.4` |
| pnpm | `10.13.1` |
| Java (legacy toolchain) | Oracle `11.0.27` |
| Java (default/backend toolchain) | Microsoft OpenJDK `17.0.15` |
| Java (Android `JAVA21_HOME`) | Eclipse Temurin `21.0.12+8` |
| Maven | `3.9.10` |
| Docker | `29.1.2` |
| Docker Compose | `2.40.3-desktop.1` |

### 4.2 MVP Standard required toolchain

These are the exact versions to be used for future development work:

| Tool | Exact version | Scope |
|---|---|---|
| Node.js | `20.19.4` | All front-end projects |
| npm | `11.6.4` | `frontend/huanzhe`, `frontend/yisheng/web-admin` |
| pnpm | `10.13.1` | `frontend/pharmacist` if pnpm is used |
| Java | `11.0.27` | Legacy backend only |
| Java | `17.0.12` | `backend-v2` build and runtime |
| JDK | Eclipse Temurin `21.0.12+8` | Patient Capacitor Android build only |
| Maven | `3.9.10` | All Java projects |
| Docker | `29.1.2` | Local container runtime |
| Docker Compose | `2.40.3-desktop.1` | Local orchestration |

### 4.3 Java policy

- Legacy backend runtime: Oracle Java `11.0.27`
- New backend standard runtime: Eclipse Temurin Java `17.0.12`
- Patient Android build toolchain: Eclipse Temurin JDK `21.0.12+8`

Notes:

- Java 11 is legacy-only.
- Any new backend service must target Java 17.
- Java 21 is used only to run Gradle and compile the Capacitor Android project; it does not change the backend Java target or the `eclipse-temurin:17-jre` runtime image baseline.
- Local development keeps the default `JAVA_HOME` on Java 17 and exposes Java 21 separately through `JAVA21_HOME`.

## 5. Legacy Frozen

### 5.1 Legacy backend identity

| Item | Exact value |
|---|---|
| Workspace path | `backend` |
| Group ID | `com.sqlapk` |
| Root artifact ID | `medicine` |
| Workspace version | `1.7.2-SNAPSHOT` |
| Java target | `11` |
| Spring Boot | `2.7.18` |

### 5.2 Legacy backend module set

Frozen modules currently present:

- `medicine-server`
- `medicine-app-server`
- `medicine-module-system`
- `medicine-module-infra`
- `medicine-module-drug`
- `medicine-module-patient`
- `medicine-module-doctor`
- `medicine-module-medication`
- `medicine-framework`
- `medicine-dependencies`

### 5.3 Legacy backend core dependency BOM

Exact BOM-managed versions:

| Dependency | Exact version |
|---|---|
| spring-boot | `2.7.18` |
| springdoc | `1.6.15` |
| knife4j | `4.1.0` |
| druid | `1.2.16` |
| mybatis-plus | `3.5.3.1` |
| mybatis-plus-generator | `3.5.3.1` |
| dynamic-datasource | `3.6.1` |
| mybatis-plus-join-boot-starter | `1.4.3.2` |
| redisson | `3.18.0` |
| lock4j | `2.2.3` |
| resilience4j | `1.7.1` |
| skywalking | `8.12.0` |
| spring-boot-admin | `2.7.10` |
| opentracing | `0.33.0` |
| flowable | `6.8.0` |
| captcha-plus | `1.0.2` |
| jsoup | `1.15.4` |
| lombok | `1.18.30` |
| mapstruct | `1.5.5.Final` |
| hutool | `5.8.18` |
| easyexcel | `3.3.1` |
| velocity | `2.3` |
| screw | `1.0.5` |
| fastjson | `1.2.83` |
| guava | `31.1-jre` |
| guice | `5.1.0` |
| transmittable-thread-local | `2.14.2` |
| commons-net | `3.8.0` |
| jsch | `0.1.55` |
| tika-core | `2.7.0` |
| netty-all | `4.1.90.Final` |
| minio | `8.5.2` |
| aliyun-java-sdk-core | `4.6.3` |
| aliyun-java-sdk-dysmsapi | `2.2.1` |
| tencentcloud-sdk-java | `3.1.715` |
| justauth | `1.4.0` |
| jimureport | `1.5.6` |
| wx-java-mp | `4.3.0` |

### 5.4 Legacy backend build plugins

Exact versions from root `backend/pom.xml`:

| Plugin | Exact version |
|---|---|
| maven-compiler-plugin | `3.11.0` |
| maven-surefire-plugin | `3.2.5` |
| maven-failsafe-plugin | `3.2.5` |
| build-helper-maven-plugin | `3.6.0` |
| maven-checkstyle-plugin | `3.3.1` |
| spotbugs-maven-plugin | `4.7.3.6` |
| pmd-maven-plugin | `3.21.0` |
| maven-enforcer-plugin | `3.3.0` |
| jacoco-maven-plugin | `0.8.10` |
| owasp-dependency-check-maven | `8.4.0` |

### 5.5 Legacy runtime images

Exact versions found in current files:

| Component | Exact value | Status |
|---|---|---|
| `medicine-server` base image | `eclipse-temurin:11-jre` | Legacy only |
| `medicine-app-server` base image | `eclipse-temurin:11-jre` | Legacy only |
| Front-end delivery image | `nginx:alpine` | Ambiguous legacy tag |
| MySQL image in compose | `mysql:8` | Ambiguous legacy tag |
| Redis image in compose | `redis:6-alpine` | Ambiguous legacy tag |

Rule:

- `nginx:alpine`, `mysql:8`, and `redis:6-alpine` are accepted only as legacy current state.
- New deployment definitions must use exact image tags.

## 6. Active front-end stack

### 6.1 Patient App

Workspace path:

- `frontend/huanzhe`

Package identity:

| Item | Exact value |
|---|---|
| package name | `ionic-app-base` |
| package version | `0.0.0` |
| lockfile version | `3` |

Runtime dependencies resolved from `package-lock.json`:

| Package | Exact version |
|---|---|
| `@capacitor/camera` | `7.0.2` |
| `@capacitor/core` | `7.4.3` |
| `@capacitor/local-notifications` | `7.0.7` |
| `@ionic/vue` | `8.7.3` |
| `@ionic/vue-router` | `8.7.3` |
| `axios` | `1.11.0` |
| `ionicons` | `7.4.0` |
| `vue` | `3.5.21` |
| `vue-router` | `4.5.1` |

Build and dev dependencies resolved from `package-lock.json`:

| Package | Exact version |
|---|---|
| `@vitejs/plugin-legacy` | `5.4.3` |
| `@vitejs/plugin-vue` | `4.6.2` |
| `@vue/eslint-config-typescript` | `12.0.0` |
| `@vue/test-utils` | `2.4.6` |
| `cypress` | `13.17.0` |
| `eslint` | `8.57.1` |
| `eslint-plugin-vue` | `9.33.0` |
| `jsdom` | `22.1.0` |
| `terser` | `5.44.0` |
| `typescript` | `5.6.3` |
| `vite` | `5.2.14` |
| `vitest` | `0.34.6` |
| `vue-tsc` | `2.2.12` |

Android build toolchain:

| Item | Exact value |
|---|---|
| Android application ID | `com.yaoanshi.patient` |
| Capacitor Android | `7.4.3` |
| Eclipse Temurin JDK | `21.0.12+8` |
| Gradle Wrapper | `8.11.1` |
| Android Gradle Plugin | `8.7.2` |
| compileSdk | `35` |
| targetSdk | `35` |
| minSdk | `23` |

Android build rules:

1. Use `JAVA21_HOME` for `npm run android:build`; do not replace the Java 17 backend default.
2. `npx cap sync android` must not leave unexplained tracked-file changes.
3. Pull Requests build an unsigned debug APK for deterministic compilation verification; release signing credentials are outside the repository and outside this gate.
4. The debug APK is retained by GitHub Actions for 14 days and must never be committed.
5. Medical offline state is native-only: an in-repository Capacitor plugin stores AES/GCM ciphertext in the application-private directory and keeps its 256-bit key in Android Keystore. Browser `localStorage`, `sessionStorage`, and IndexedDB are forbidden for that state.
6. Local notification lock-screen text must remain generic and must not include patient identity, medicine name, or dose.
7. The packaged Android application keeps the default secure `https://localhost` WebView origin. The debug build script may set `CAPACITOR_ANDROID_LOCAL_HTTP=true` to use `http://localhost` with ADB reverse for local-device acceptance; this override must not enter release builds.

Standard route/API binding:

- Front-end dev port: `3001`
- Back-end API prefix: `/app-api/**`
- Primary login mode: SMS code login

### 6.2 Pharmacist Web

Workspace path:

- `frontend/pharmacist`

Package identity:

| Item | Exact value |
|---|---|
| package name | `yudao-ui-admin-vue3` |
| package version | `1.7.3-snapshot` |
| lockfile version | `3` |
| package manager declared | `pnpm@8.6.0` |
| node engine declared | `>= 16.0.0` |

Runtime dependencies resolved from `package-lock.json`:

| Package | Exact version |
|---|---|
| `@ditdot-dev/vue-flow-form` | `2.3.2` |
| `@element-plus/icons-vue` | `2.3.2` |
| `@form-create/designer` | `3.3.0` |
| `@form-create/element-ui` | `3.2.30` |
| `@iconify/iconify` | `3.1.1` |
| `@videojs-player/vue` | `1.0.0` |
| `@vueuse/core` | `10.11.1` |
| `@wangeditor/editor` | `5.1.23` |
| `@wangeditor/editor-for-vue` | `5.1.12` |
| `@zxcvbn-ts/core` | `3.0.4` |
| `animate.css` | `4.1.1` |
| `axios` | `1.11.0` |
| `benz-amr-recorder` | `1.1.5` |
| `bpmn-js-token-simulation` | `0.10.0` |
| `camunda-bpmn-moddle` | `7.0.1` |
| `cropperjs` | `1.6.2` |
| `crypto-js` | `4.2.0` |
| `dayjs` | `1.11.18` |
| `diagram-js` | `11.13.1` |
| `echarts` | `5.6.0` |
| `echarts-wordcloud` | `2.1.0` |
| `element-plus` | `2.3.7` |
| `fast-xml-parser` | `4.5.3` |
| `highlight.js` | `11.11.1` |
| `intro.js` | `7.2.0` |
| `jsencrypt` | `3.5.4` |
| `lodash-es` | `4.17.21` |
| `min-dash` | `4.2.3` |
| `mitt` | `3.0.1` |
| `nprogress` | `0.2.0` |
| `pinia` | `2.3.1` |
| `qrcode` | `1.5.4` |
| `qs` | `6.14.0` |
| `steady-xml` | `0.1.0` |
| `survey-core` | `1.12.52` |
| `survey-vue` | `1.12.52` |
| `survey-vue3-ui` | `1.12.52` |
| `url` | `0.11.4` |
| `video.js` | `8.23.4` |
| `vue` | `3.3.4` |
| `vue-demi` | `0.14.10` |
| `vue-i18n` | `9.2.2` |
| `vue-router` | `4.5.1` |
| `vue-types` | `5.1.3` |
| `vuedraggable` | `4.1.0` |
| `web-storage-cache` | `1.1.1` |
| `xml-js` | `1.6.11` |

Build and dev dependencies resolved from `package-lock.json`:

| Package | Exact version |
|---|---|
| `@commitlint/cli` | `17.8.1` |
| `@commitlint/config-conventional` | `17.8.1` |
| `@iconify/json` | `2.2.380` |
| `@intlify/unplugin-vue-i18n` | `0.11.0` |
| `@purge-icons/generated` | `0.9.0` |
| `@types/intro.js` | `5.1.5` |
| `@types/lodash-es` | `4.17.12` |
| `@types/node` | `20.19.12` |
| `@types/nprogress` | `0.2.3` |
| `@types/qrcode` | `1.5.5` |
| `@types/qs` | `6.14.0` |
| `@typescript-eslint/eslint-plugin` | `5.62.0` |
| `@typescript-eslint/parser` | `5.62.0` |
| `@vitejs/plugin-legacy` | `4.1.1` |
| `@vitejs/plugin-vue` | `5.2.4` |
| `@vitejs/plugin-vue-jsx` | `3.1.0` |
| `autoprefixer` | `10.4.21` |
| `bpmn-js` | `8.10.0` |
| `bpmn-js-properties-panel` | `0.46.0` |
| `consola` | `3.4.2` |
| `eslint` | `8.57.1` |
| `eslint-config-prettier` | `8.10.2` |
| `eslint-define-config` | `1.24.1` |
| `eslint-plugin-prettier` | `4.2.5` |
| `eslint-plugin-vue` | `9.33.0` |
| `lint-staged` | `13.3.0` |
| `postcss` | `8.5.6` |
| `postcss-html` | `1.8.0` |
| `postcss-scss` | `4.0.9` |
| `prettier` | `2.8.8` |
| `rimraf` | `5.0.10` |
| `rollup` | `3.29.5` |
| `sass` | `1.98.0` |
| `stylelint` | `15.11.0` |
| `stylelint-config-recommended` | `12.0.0` |
| `stylelint-config-recommended-vue` | `1.6.1` |
| `stylelint-config-standard` | `33.0.0` |
| `stylelint-order` | `6.0.4` |
| `terser` | `5.44.0` |
| `typescript` | `5.0.4` |
| `unplugin-auto-import` | `0.16.7` |
| `unplugin-element-plus` | `0.7.2` |
| `unplugin-vue-components` | `0.25.2` |
| `vite` | `6.4.1` |
| `vite-plugin-compression` | `0.5.1` |
| `vite-plugin-ejs` | `1.7.0` |
| `vite-plugin-eslint` | `1.8.1` |
| `vite-plugin-progress` | `0.0.7` |
| `vite-plugin-purge-icons` | `0.9.2` |
| `vite-plugin-svg-icons` | `2.0.1` |
| `vite-plugin-top-level-await` | `1.6.0` |
| `vite-plugin-windicss` | `1.9.4` |
| `vue-tsc` | `1.8.27` |
| `windicss` | `3.5.6` |

Standard route/API binding:

- Front-end dev port: `3003`
- Back-end API prefix: `/admin-api/**`
- Realtime channel: WebSocket

### 6.3 Doctor Web

Workspace path:

- `frontend/yisheng/web-admin`

Package identity:

| Item | Exact value |
|---|---|
| package name | `medical-pharmacy-admin` |
| package version | `1.0.0` |
| lockfile version | `3` |

Runtime dependencies resolved from `package-lock.json`:

| Package | Exact version |
|---|---|
| `vue` | `3.5.21` |
| `vue-router` | `4.5.1` |
| `pinia` | `2.3.1` |
| `element-plus` | `2.11.2` |
| `@element-plus/icons-vue` | `2.3.2` |
| `axios` | `1.11.0` |

Build and dev dependencies resolved from `package-lock.json`:

| Package | Exact version |
|---|---|
| `@vitejs/plugin-vue` | `5.2.4` |
| `vite` | `6.4.1` |
| `sass` | `1.98.0` |

Standard route/API binding:

- Front-end dev port: `3002`
- Back-end API prefix: `/admin-api/**`
- Realtime channel: WebSocket

## 7. MVP Standard for new backend work

This section is the required stack for future `backend-v2` work.

Current status:

- `backendV2/pom.xml` does not exist yet.
- Therefore this section is a standard decision baseline, not an active manifest snapshot.

### 7.1 Identity

| Item | Exact value |
|---|---|
| Service name | `backend-v2` |
| Java | `17.0.12` |
| Maven | `3.9.10` |
| Packaging style | `Maven single service` |
| Architecture style | `modular monolith` |

### 7.2 Required framework set

| Dependency | Exact version policy |
|---|---|
| Spring Boot | `3.2.12` |
| Spring Framework / Spring Security | `managed by Spring Boot 3.2.12 BOM only` |
| Spring Web | `managed by Spring Boot 3.2.12 BOM only` |
| Spring Security | `managed by Spring Boot 3.2.12 BOM only` |
| Spring Data Redis | `managed by Spring Boot 3.2.12 BOM only` |
| MyBatis-Plus Spring Boot 3 Starter | `3.5.7` |
| SpringDoc OpenAPI Starter WebMVC UI | `2.5.0` |

### 7.3 Required backend capabilities

These capabilities are mandatory for `backend-v2`:

1. Global exception handling
2. Unified `CommonResult<T>`
3. JWT authentication
4. Redis integration
5. OpenAPI documentation
6. `/health` endpoint

### 7.4 Required package structure

Exact package root:

`com.medicine`

Required top-level packages:

- `com.medicine.api`
- `com.medicine.service`
- `com.medicine.repository`
- `com.medicine.domain`
- `com.medicine.config`
- `com.medicine.common`

## 8. Runtime image standard for new work

The following exact image versions are required for new deployment files:

| Component | Exact version |
|---|---|
| Java runtime base image | `eclipse-temurin:17-jre` |
| MySQL | `mysql:8.0.36` |
| Redis | `redis:6.2.14-alpine` |
| Nginx | `nginx:1.27.0-alpine` |

Rule:

- New compose files and Dockerfiles must use exact tags from this section.
- Legacy compose files may keep old tags until they are replaced.

## 9. Package manager rules

1. Patient App uses `npm` with `package-lock.json` as the exact source of truth.
2. Doctor Web uses `npm` with `package-lock.json` as the exact source of truth.
3. Pharmacist Web may still run with npm in the current repo, but the declared package manager baseline is `pnpm@8.6.0`.
4. No package may be upgraded by range-only editing without regenerating the lockfile.

## 10. Version freeze rules

1. Any dependency upgrade must update this file in the same change.
2. Any API prefix change must update this file in the same change.
3. Any port change must update this file in the same change.
4. Any new package introduced into MVP must be added here with an exact version.
5. Any new Docker base image must use an exact tag.

## 11. Immediate interpretation for implementation

The effective standard to follow now is:

1. Legacy code in `backend` stays frozen on Java 11 / Spring Boot 2.7.18.
2. Patient App development follows the exact locked versions listed for `frontend/huanzhe`.
3. Pharmacist Web development follows the exact locked versions listed for `frontend/pharmacist`.
4. Doctor Web development follows the exact locked versions listed for `frontend/yisheng/web-admin`.
5. Any new backend implementation must target the `backend-v2` standard in Section 7 and Section 8.
