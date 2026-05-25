# Product Requirement Document

---

# Project Overview

## Project Name
webGame-contextTRPG

## Project Objective
컨텍스트 기반 TRPG 웹 게임 구현으로, 유저가 자유롭게 작성한 내용을 바탕으로 게임을 진행할 수 있는 기반형 웹사이트

---

# User Requirements

## What do you want to build?
웹사이트 제작 및 TRPG stat, status, map, game rule 적용 등 여러 요소를 유저가 임의로 설정하고, 다른 사람들과 함께 TRPG를 즐길 수 있는 커스터마이징 가능한 웹사이트

## Why are you building this?
기존 TRPG 사이트들과 차별화된, 완전한 커스텀이 가능한 나만의 TRPG 플랫폼을 제작하기 위해

## Target Users
TRPG를 즐기고 싶은 일반 사용자

---

# Core Features

## Required Features

- [ ] 로그인 / 회원가입 (JWT 인증)
- [ ] 채팅 기능
- [ ] 파일 업로드 (캐릭터 시트, 맵 파일 등)
- [ ] BGM 재생 기능
- [ ] TRPG 캐릭터 시트 제작 (Excel / 스크립트 파일 등)
- [ ] 맵 제작 기능
- [ ] 본인이 만든 TRPG 공유 페이지
- [ ] 관리자용 페이지

## Priority Feature
캐릭터 시트 제작, 맵 제작 기능

---

# Detailed Feature Requirements

## 캐릭터 시트 제작

### Purpose
유저가 자신의 TRPG 캐릭터 정보(stat, status 등)를 자유롭게 정의하고 저장

### Expected Behavior
유저가 Excel 파일 또는 스크립트 형식으로 캐릭터 시트를 업로드하거나 직접 에디터에서 작성 가능

### Input
Excel 파일 / 스크립트 파일 / 직접 입력 폼

### Output
저장된 캐릭터 시트 (조회 및 게임 내 활용 가능)

### Dependencies
- 로그인 / 인증 기능
- 파일 업로드 기능

---

## 맵 제작 기능

### Purpose
유저가 TRPG 진행에 사용할 맵을 직접 제작하고 관리

### Expected Behavior
맵 에디터를 통해 타일 또는 이미지 기반 맵을 제작하고 저장, 게임 세션에 연동 가능

### Input
맵 파일 업로드 또는 에디터 내 직접 제작

### Output
저장된 맵 데이터 (게임 세션에서 사용 가능)

### Dependencies
- 로그인 / 인증 기능
- 파일 업로드 기능

---

## 채팅 기능

### Purpose
TRPG 세션 참가자들 간의 실시간 소통

### Expected Behavior
게임 세션 내 실시간 채팅, 주사위 결과 공유, 마스터 공지 등

### Input
텍스트 메시지

### Output
실시간 채팅 메시지 표시

### Dependencies
- 로그인 / 인증 기능

---

## BGM 재생 기능

### Purpose
게임 분위기 조성을 위한 배경음악 재생

### Expected Behavior
마스터 또는 참가자가 BGM 파일을 업로드하고 세션 중 재생/정지 가능

### Input
오디오 파일 업로드

### Output
세션 내 BGM 재생

### Dependencies
- 파일 업로드 기능

---

## TRPG 공유 페이지

### Purpose
유저가 제작한 TRPG 설정(룰, 캐릭터 시트 양식, 맵 등)을 커뮤니티에 공유

### Expected Behavior
공유 목록 조회, 다운로드 및 즐겨찾기 가능

### Input
공유할 TRPG 패키지 (룰셋, 시트 등)

### Output
공유 페이지 게시물

### Dependencies
- 로그인 / 인증 기능

---

## 관리자 페이지

### Purpose
서비스 운영을 위한 관리자 전용 대시보드

### Expected Behavior
유저 관리, 신고 처리, 공유 콘텐츠 관리 등

### Input
관리자 계정 로그인

### Output
관리 기능 UI

### Dependencies
- 로그인 / 인증 기능 (관리자 권한 분리)

---

# UI / UX Requirements

깔끔하고 사이버펑크한 디자인을 기본으로 한다.
어두운 배경, 네온 컬러 포인트, 미래지향적 폰트 및 레이아웃 적용.

---

# Technical Requirements

## Preferred Language
Java

## Preferred Framework
미정

## Database
미정

## Infrastructure
개인 환경 (로컬 서버)

---

# External Tools / APIs

| Tool/API | Purpose |
|---|---|
| Discord (추후 확장) | 플레이어 연동 — Discord 계정으로 로그인하거나 세션 알림 연동 |

---

# Development Rules

## Coding Style
미정 (추후 Coding Rule.txt 작성 시 확정)

## Architecture Style
MVC (Model - View - Controller)

---

# Performance Requirements

## Expected Scale
없음 (개인 프로젝트, 소규모)

## Optimization Priority
(추후 작성)

---

# Security Requirements

- JWT 인증 기반 로그인 구현
- 토큰 만료 및 갱신 정책 적용
- 관리자 / 일반 유저 권한 분리

---

# Future Plans

추후 추가 예정 (사용자가 직접 확장할 계획)

---

# Boss AI Instructions

Boss AI must:

1. Analyze all requirements.
2. Split tasks into smaller units.
3. Assign suitable AI for each task.
4. Request HR AI when additional AI is needed.
5. Follow Coding Rule.txt strictly.
6. Prioritize stability and readability.
7. Generate task workflow before development starts.

---

# Final Notes
