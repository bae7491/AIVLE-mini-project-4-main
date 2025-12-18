<div align="center">
  <h1>미니 프로젝트 4차 - 부산/경남 10반</h1>
  <h3>📖 도서관리시스템 개발 📖</h3>
</div>

<br/>
<div align="center">
  <a href="https://www.notion.so/AIVLE-School-4-2bf994ebe25d80e386a7d101c97ffd0d">Notion</a>
  &nbsp; | &nbsp; 
  <a href="https://www.figma.com/design/r3jpdOz2xgUvo7j7NIQbUV/4%EC%B0%A8-%EB%AF%B8%EB%8B%88?node-id=0-1&p=f/">Figma</a>
  &nbsp; | &nbsp; 
  <a href="https://docs.google.com/spreadsheets/d/1El2XurvM0hBwo7mui5dlEC87zFCsDXSg/edit?gid=1349883987#gid=1349883987">API 명세서</a>
</div>

## ✍️ 프로젝트 개요

---

- **KT Aivle School 4차 미니프로젝트**
- **프로젝트 명: 도서관리시스템 (AI를 활용한 도서표지 이미지 생성)** 
- **프로젝트 기간: 2025.12.04(목) ~ 2025.12.09(화)**

## ✍️ 프로젝트 소개

---

### 프로젝트 배경

---

본 프로젝트는 디지털 콘텐츠와 AI 기술의 발전으로 개인 창작자 중심의 출판 및 콘텐츠 제작 환경이 확산됨에 따라, 누구나 쉽게 도서를 등록하고 관리할 수 있는 플랫폼의 필요성에서 출발하였습니다.  
특히 기존 도서 관리 시스템이 단순한 정보 저장에 그치는 한계를 보이는 반면, 본 프로젝트는 AI를 활용한 도서 표지 이미지 자동 생성 기능을 결합하여 창작자의 기획 부담을 줄이고 시각적 완성도를 높이고자 하였습니다.  
또한 JWT 기반 인증과 보안 필터를 적용하여 안전한 사용자 인증 환경을 구축하고, 효율적인 도서 등록·조회·수정·삭제가 가능한 통합 도서 관리 시스템을 구현하는 것을 목표로 개발되었습니다.

## 🚀 프로젝트 목표

- Spring Boot와 React & NEXT.js를 활용한 풀스택 웹 애리케이션 개발 역량 강화.
- REST API 설계 및 구현.
- JPA를 이용한 데이터베이스 연동.
- 외부 API (OpenAI) 연동.

---

## 📌 주요 기능

1. **JWT 기반 회원 인증 기능**: 회원가입, 로그인, 회원탈퇴를 통해 사용자 인증과 권한을 안전하게 관리합니다.
2. **도서 등록 기능**: 인증된 사용자가 도서의 제목, 설명, 내용, 카테고리를 등록할 수 있으며 작성자는 JWT 기반으로 자동 설정됩니다.
3. **도서 목록 조회 및 페이징 처리**: 다수의 도서를 페이지 단위로 조회할 수 있으며, 현재 페이지와 전체 페이지 수 정보를 함께 제공합니다.
4. **도서 수정 및 삭제 기능**: 인증된 도서 작성자 본인의 도서 정보를 수정하거나 삭제할 수 있습니다.
5. **AI 기반 도서 표지 이미지 생성 기능**: 도서의 제목과 설명을 기반으로 AI가 자동으로 표지 이미지를 생성하고 저장합니다.
6. **공통 API 응답 구조 적용**: 모든 API 응답을 `status`, `message`, `data` 형식의 공통 응답 구조로 통일하여 관리합니다.
7. **H2 기반 데이터베이스 관리**: H2 파일 기반 DB를 사용하여 도서, 사용자, 카테고리, Refresh Token 데이터를 관리합니다.
8. **보안 필터 기반 인증 처리**: JWT 검증을 필터에서 일괄 처리하여 컨트롤러와 서비스의 보안 로직을 분리합니다.

---

## 🧑‍💻 팀원 소개

|           **양시현**            |   **박기준**    |    **배교성**    | **신다훈**  |
|:----------------------------:|:------------:|:-------------:|:--------:|
|            팀장, BE            |    팀원, FE    |    팀원, BE     |  팀원, FE  |
| 회원 관리 API, <br> JWT 기반 회원 인증 | 작품 목록 페이지 생성 | 도서 및 카테고리 API | 메인 화면 UI |

|       **이연호**        |         **이정제**          |    **이태훈**    | **장석진**  |
|:--------------------:|:------------------------:|:-------------:|:--------:|
|        팀원, FE        |          팀원, FE          |    팀원, BE     |  팀원, FE  |
| 도서 상세 보기, <br> 도서 편집 | AI 기반 이미지 생성,<br> 게시물 구성 | 도서 및 카테고리 API | 회원 관련 UI |

---
## ⚙️ 기술 스택

<table>
    <thead>
        <tr>
            <th>분류</th>
            <th>기술 스택</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>
                  <p>프론트엔드</p>
            </td>
            <td>
                  <img src="https://img.shields.io/badge/React-61DAFB?style=flat&logo=react&logoColor=white"/>
                  <img src="https://img.shields.io/badge/CSS-663399?style=flat&logo=css&logoColor=white"/>
                  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=flat&logo=JavaScript&logoColor=white"/>
                  <img src="https://img.shields.io/badge/NEXT.js-000000?style=flat&logo=Next.js&logoColor=white"/>
                  <img src="https://img.shields.io/badge/Bootstrap-7952B3?style=flat&logo=Bootstrap&logoColor=white"/>
                  <img src="https://img.shields.io/badge/MUI-007FFF?style=flat&logo=MUI&logoColor=white"/>
            </td>
        </tr>
        <tr>
            <td>
                <p>백엔드</p>
            </td>
            <td>
                <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white"/>
                <img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=Java&logoColor=white"/>
                <img src="https://img.shields.io/badge/Gradle-02303A?style=flat&logo=gradle&logoColor=white"/>
                <img src="https://img.shields.io/badge/H2 Database-09476B?style=flat&logo=H2 Database&logoColor=white"/>
            </td>
        </tr>
        <tr>
            <td>
                <p>외부 API</p>
            </td>
            <td>
                <img src="https://shields.io/badge/OpenAI-93f6ef?style=flat&logo=openai&logoColor=white"/>
            </td>
        </tr>
        <tr>
            <td>
                <p>협업 툴</p>
            </td>
            <td>
                <img src="https://img.shields.io/badge/Git-F05032?style=flat&logo=git&logoColor=white"/>
                <img src="https://img.shields.io/badge/Notion-000000?style=flat&logo=notion&logoColor=white"/>
                <img src="https://img.shields.io/badge/Figma-F24E1E?style=flat&logo=Figma&logoColor=white"/>
            </td>
        </tr>
    </tbody>
</table>

---

### 회원 관리 기능
- **회원 가입**  
  ![회원 가입](docs/front_ui/sign_up.gif)

- **회원 정보 수정**  
  ![회원 정보 수정](docs/front_ui/user_modify.gif)

- **회원 탈퇴**  
  ![회원 탈퇴](docs/front_ui/sign_out.gif)

### 게시글 관련 기능
- **도서 등록**  
  ![도서 등록](docs/front_ui/gif_2.gif)

- **도서 AI 표지 생성**  
  ![도서 AI 표지 생성](docs/front_ui/gif_1.gif)

- **도서 수정**  
  ![도서 수정](docs/front_ui/도서_수정.gif)

## Backend 개요
이 백엔드는 도서 작성/조회/수정/삭제 기능과 회원 인증(JWT)을 제공하는 REST API 서버입니다.  
프론트엔드가 호출하는 HTTP API를 통해 도서 콘텐츠 서비스를 지원합니다.

- **회원 관리 기능**
    - 회원가입 API를 통해 아이디·비밀번호·이름을 입력받아 새로운 회원을 생성한다.
    - JWT Access Token을 이용해 인증된 사용자의 이름·비밀번호·API Key를 수정하는 회원정보 수정 기능을 구현했다.
    - 비밀번호를 한 번 더 확인한 뒤, 본인 계정을 시스템에서 삭제하는 회원탈퇴 로직을 담당했다.

- **로그인·로그아웃 & 토큰 관리**
    - 아이디·비밀번호를 검증해 로그인 처리하고, 성공 시 Access Token과 Refresh Token을 함께 발급하는 인증 흐름을 구현했다.
    - 클라이언트가 전달한 Access Token을 무효화하거나 Refresh Token을 삭제하는 방식으로 로그아웃 기능을 구현했다.
    - Access Token의 만료 여부·위변조 여부를 확인하는 토큰 유효성 검사 API를 제공한다.
    - 쿠키에 저장된 Refresh Token을 사용해 새로운 Access Token을 재발급하는 토큰 재발급 기능을 개발했다.

- **JWT 기반 사용자/키 조회**
    - JWT Access Token에서 사용자 정보를 추출한 뒤, 해당 사용자에게 발급된 API Key를 조회해 반환하는 API를 구현했다.
    - 유효한 토큰을 전제로, 사용자 ID와 이름을 조회해 내려주는 “JWT 기반 사용자 정보 조회” 기능을 담당했다.

- **도서 목록 & 검색**
    - 페이지 번호를 기준으로 도서 목록을 조회하고, 페이지/전체 페이지 수/전체 아이템 수를 함께 내려주는 리스트 API를 구현했다.
    - 사용자가 입력한 검색어가 도서 제목에 포함된 경우만 필터링하여, 제목 기반 검색 결과를 페이징 형태로 반환하는 검색 API를 개발했다.

- **도서 등록·수정**
    - JWT 인증된 사용자가 제목, 설명, 본문, 카테고리, 이미지 URL 등을 포함해 새로운 도서를 등록하는 API를 구현했다.
    - 도서를 작성한 사용자만 자신의 도서 정보를 수정할 수 있도록 권한을 체크하고, 제목·내용·카테고리·이미지 등을 수정하는 로직을 담당했다.

- **카테고리 조회**
    - DB에 등록된 전체 카테고리 목록을 조회해, 카테고리 ID와 이름을 배열 형태로 반환하는 카테고리 조회 API를 구현했다.
    - 프론트엔드 드롭다운·필터에서 바로 사용할 수 있도록 응답 구조를 단순하게 설계했다.

- **도서 상세 조회**
    - 도서 ID를 입력받아 제목, 설명, 본문, 카테고리 ID, 이미지 URL, 작성자 ID, 생성/수정일자를 포함한 도서 상세 정보를 반환하는 API를 구현했다.
    - 존재하지 않는 도서 ID로 요청이 들어오면 “도서를 찾을 수 없습니다.” 에러 메시지를 내려주는 예외 처리 로직을 함께 담당했다.

- **도서 삭제**
    - JWT로 인증된 사용자 중, 해당 도서를 작성한 사용자만 삭제할 수 있도록 권한 체크 후 도서를 삭제하는 API를 구현했다.
    - 삭제 결과로 삭제된 도서 ID와 삭제 여부를 반환해, 프론트에서 후속 처리를 쉽게 할 수 있도록 했다.

- **카테고리·목록 보완 & 커버 이미지**
    - 도서 목록 및 카테고리 조회 로직을 다른 팀원과 함께 보완하면서, 응답 형식을 통일하고 공통 DTO 구조에 맞게 정리했다.
    - 도서 ID를 기반으로 커버 이미지 URL을 조회해 반환하는 API를 구현해, 상세 페이지에서 표지 이미지를 바로 사용할 수 있게 했다.

## 🔨 시스템 아키텍처

![System Architecture](./docs/System_Architecture.png)

---

## 📊 ERD

![ERD Diagram](./docs/ERD.png)

---