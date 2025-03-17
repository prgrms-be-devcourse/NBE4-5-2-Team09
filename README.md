# NBE4-5-2-Team09

프로그래머스 백엔드 데브코스 4기 5회차 9팀 시고르백구의 2차 팀 프로젝트입니다.


## 👥 Team Introduction

| Name     | GitHub                                    | Role       |
|----------|---------------------------------------------|------------|
| 이화영 | [2hwayoung](https://github.com/2hwayoung)  | **팀장**   |
| 김하연 | [xaxeon](https://github.com/xaxeon)        | 팀원       |
| 이승민 | [min429](https://github.com/min429)        | 팀원       |
| 장무영 | [wkdan](https://github.com/wkdan)          | 팀원       |


## 📊 Project Overview

### 프로젝트 이름: **코잉(Coing)**
💡 *실시간 코인 데이터 분석 및 북마크 대시보드 서비스*

#### 관련 문서
- [📘 1차 MVP 기획서](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/%EC%BD%94%EC%9E%89(Coing)-1%EC%B0%A8-MVP-%EA%B8%B0%ED%9A%8D%EC%84%9C)
- [📝 코잉(Coing) 기획서](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/%EC%BD%94%EC%9E%89(Coing)-%EA%B8%B0%ED%9A%8D%EC%84%9C)


### Project Background
 - 업비트 OpenAPI와 WebSocket을 활용해 코인 관련 중요 지표(현재가, 체결자, 호가, 캔들 차트)를 실시간으로 안정적으로 제공

 - 사용자가 관심 있는 코인을 북마크하여 개인화된 대시보드를 구성할 수 있는 사이트 개발

 - 기존의 단순 정보 제공 서비스와 달리, 다양한 기술적 지표(Spread, Imbalance, Liquidity Depth 등)를 통해 보다 전문적인 분석 도구 제공
 

### Key Features
 - 일반 회원 기능:
    - 회원가입
    - 로그인
    - 로그아웃
 - 코인 대시보드:
    - 목록 조회 (필터링/정렬: 인기순, 시가순, 변동폭, 거래량 등)
    - 시세 캔들 차트 조회 (초/분/일/주/월/년 단위)
    - 종목/마켓 단위 현재가, 체결가 및 호가 정보 조회
 - 북마크 대시보드:
    - 북마크 등록/삭제
    - 북마크한 코인 목록 조회 (필터링/정렬: 인기순, 시가순, 변동폭, 거래량 등)
    - 종목/마켓 단위 현재가, 체결가 및 호가 정보 조회
 - 추가 기능 (향후 확장)
    - 소셜 회원가입/로그인
    - 코인 항목 별 관련 뉴스 실시간 집계\
      

## 🛠️ Development Setup

**Run Database (Docker Compose)**

```bash
# Start MySQL container with Docker Compose
# in root directory
docker-compose up -d

# Monitoring Logs
# Since logs are mapped to your local machine in ./mysql/conf, you can monitor them directly:
tail -f ./mysql_logs/general.log

# Stop Containers
docker-compose down

```

**Run Frontend (Next.js)**

```bash
# Navigate to frontend project directory
cd frontend

# Start Next.js development server
npm install  # Install dependencies (only needed once)
npm run dev  # Start development server

# Use OpenAPI to generate TypeScript types for the backend API
npm run codegen # Generate openapi typeScript definitions
npm run codegen:watch # Watch for API changes and regenerate types automatically

```

## Database Settings

애플리케이션에서 데이터베이스 설정을 환경별 파일로 분리하여, 각 환경에 맞는 DB 연결 정보, JPA ddl-auto, 로깅 레벨 등을 세팅했습니다.

- prod: 운영환경 – MySQL 사용
- dev: 개발환경 – Docker Compose를 통해 MySQL 사용
- test: 테스트 환경 – In-memory H2 Database 사용

또한, 운영 환경에서는 향후 데이터베이스 스키마 마이그레이션을 위해 Flyway를 도입할 예정입니다. (현재는 JPA의 ddl-auto: update를 사용하며 Flyway는 비활성화 상태입니다.)


## Architecture

### System Architecture
- [💻 시스템 구성도](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/%EC%8B%9C%EC%8A%A4%ED%85%9C-%EA%B5%AC%EC%84%B1%EB%8F%84(%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98))
- [📃 프로젝트 구조](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EA%B5%AC%EC%A1%B0)

### 🛠️ Technology Stack

#### 🎨 Frontend
<div align=""> 
  <img src="https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white"/>
  <img src="https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white"/>
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"/>
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white"/>
  <img src="https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white"/>
</div>

#### 🛠 Backend
<div align=""> 
  <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Apache%20Tomcat-F8DC75?style=for-the-badge&logo=apachetomcat&logoColor=white"/>
</div>

#### 🗄 Database
<div align=""> 
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
</div>

#### 🚀 Deployment & Infra
<div align=""> 
  <img src="https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black"/>
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white"/>
  <img src="https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white"/>
  <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"/>
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white"/>
</div>

---

## 📡 API & Communication
- **Upbit API:** 실시간 WebSocket 및 REST API 연동
- **Naver Search API:** 코인 관련 실시간 뉴스 데이터 제공


## 🚀 Deployment & CI/CD
- [🔗 배포 및 자동화 가이드](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/%EB%B0%B0%ED%8F%AC-%EB%B0%8F-%EC%9E%90%EB%8F%99%ED%99%94)


## 🗃 ERD
- [📊 DB ERD](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/ERD)


## 📌 Convention
- [📑 Project Convention](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/Convention)
- [📝 Git Convention](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/Git-Hub-%ED%98%91%EC%97%85-%EA%B7%9C%EC%B9%99)


## ❓ Etc
- [🛠️ TroubleShootings](https://github.com/prgrms-be-devcourse/NBE4-5-2-Team09/wiki/TroubleShootings)
