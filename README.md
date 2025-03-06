# NBE4-5-2-Team09

프로그래머스 백엔드 데브코스 4기 5회차 9팀 시고르백구의 2차 팀 프로젝트입니다.

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

### Upbit WebSocket API

Upbit API와의 실시간 WebSocket 연결을 구현하여, 주문서 데이터를 안정적으로 수신하고 처리하는 아키텍처를 구성했습니다.
