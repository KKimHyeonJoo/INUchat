# 🎓 인챗 (InChat) - 인천대학교 AI 챗봇 '횃불이'

> **2024 인천대학교 캡스톤 디자인 프로젝트 - 장려상 수상작** 🏆  
> RAG(Retrieval-Augmented Generation) 기반의 학교 홈페이지 및 PDF 문서 실시간 검색 및 답변 챗봇

<br>

## 📖 프로젝트 소개 (Project Overview)

\*\*인챗(InChat)\*\*은 인천대학교 학생들이 학교 정보를 찾을 때 겪는 불필요한 검색 시간 소요와 야간/휴일 문의 불가 문제를 해결하기 위해 개발된 **GPT 기반 AI 챗봇 애플리케이션**입니다.

기존의 시나리오형 챗봇과 달리, 학교 홈페이지의 **PDF 문서들을 지식 베이스로 활용**하여 답변을 생성하므로 최신 정보를 실시간으로 반영할 수 있으며, 유학생들을 위해 **한국어와 영어를 모두 지원**합니다.

### 🎯 기획 의도 및 주요 목표

  * **정보 검색 효율화:** 웹서핑 없이 자연어 질문만으로 학사 정보(장학금, 학과 사무실 위치, 수강신청 등) 획득
  * **정보 접근성 강화:** 한국어에 서툰 유학생 및 신입생을 위한 다국어(한/영) 지원
  * **24/7 무중단 서비스:** 업무 시간 외에도 언제든지 질의응답 가능

<br>

## ✨ 주요 기능 (Key Features)

1.  **RAG 기반 정보 검색:** 사용자의 질문과 가장 유사한 교내 규정/공지 PDF 문서를 검색(Retriever)하여 GPT가 정확한 답변을 생성
2.  **다국어 지원 (Kor/Eng):** 사용자가 선택한 언어(한국어/영어)에 맞춰 프롬프트 및 답변 최적화
3.  **동의어/약어 처리:** '과사(학과 사무실)', '학식(학생 식당)' 등 학생들이 자주 쓰는 줄임말을 정확한 용어로 인식하여 처리
4.  **문맥 인식:** 이전 대화 맥락을 고려하거나, 문서 내의 구체적인 수치/장소 정보를 정확히 추출

<br>

## 🛠 기술 스택 (Tech Stack)

### 📱 Client (Android)

  * **Language:** Java
  * **IDE:** Android Studio
  * **Networking:** OkHttp3 (비동기 통신)
  * **UI:** RecyclerView, Linear Layout (채팅 인터페이스 구현)

### 🖥️ Server (Backend)

  * **Language:** Python
  * **Framework:** Flask
  * **Deploy:** AWS (EC2)

### 🧠 AI & NLP

  * **LLM:** OpenAI GPT-3.5-turbo
  * **Orchestration:** LangChain
  * **Embedding:** OpenAI Embeddings
  * **Vector Store:** FAISS (Facebook AI Similarity Search)
  * **Document Loader:** PyPDFLoader

<br>

## ⚙️ 시스템 아키텍처 (System Architecture)

본 프로젝트는 **Android 클라이언트**와 **Flask 서버** 간의 통신으로 이루어지며, 서버 내부적으로 **RAG 파이프라인**이 동작합니다.

flowchart TD
    %% 사용자 및 추론 영역 (기존 유지)
    subgraph Inference_Phase ["🔍 기존 검색 및 답변 영역"]
        User(👤 User) -->|Question| Retriever
        Retriever -->|Search| FAISS[(🗄️ FAISS Vector Store)]
        FAISS -->|Context| LLM[🤖 LLM (gpt-3.5-turbo)]
        User -->|Prompt| LLM
        LLM --> Answer[📝 Answer]
    end

    %% 데이터 수집 및 가공 영역 (신규 추가 및 수정)
    subgraph ETL_Pipeline ["⚙️ 주기적 업데이트 (Batch Indexing) 파이프라인"]
        Scheduler(⏰ Scheduler\n매일/매시간 트리거) -->|Start| Crawler
        
        subgraph Collection ["데이터 수집"]
            Web[🏫 학교 공지사항\nWebsite] -->|HTTP Get| Crawler[🕷️ Web Crawler\nScraper]
        end
        
        Crawler -->|Raw HTML| Dedup{♻️ 중복 제거\nDe-duplication}
        
        Dedup -->|New Content| Cleaner[🧹 Data Cleaner\n텍스트 추출]
        Dedup -->|Exists| Skip[⛔ Skip]
        
        Cleaner -->|Clean Text| Splitter[📄 Text Splitter\nLangChain]
        
        Splitter -->|Chunks| Embed[🧠 OpenAI Embeddings]
        Embed -->|Upsert Vectors| FAISS
    end

    %% 스타일링
    style Scheduler fill:#f9f,stroke:#333,stroke-width:2px
    style Crawler fill:#bbf,stroke:#333,stroke-width:2px
    style Dedup fill:#ff9,stroke:#333,stroke-width:2px
    style FAISS fill:#ddd,stroke:#333,stroke-width:4px

### RAG 파이프라인 상세

1.  **Document Loading:** 학교 홈페이지의 PDF 파일들을 `PyPDFLoader`로 로드
2.  **Splitting:** 문맥 유지를 고려하여 텍스트 분할 (LangChain TextSplitter)
3.  **Embedding & Storage:** `OpenAI Embeddings`로 벡터화하여 `FAISS`에 저장
4.  **Retrieval:** 사용자 질문(Query)과 유사도가 높은 문서 청크(Chunk) 검색
5.  **Generation:** 검색된 문서(Context)와 질문을 프롬프트에 주입하여 GPT가 답변 생성

<br>

## 📊 성능 평가 및 성과 (Performance & Results)

프로젝트 과정에서 최적의 모델과 검색 방식을 선정하기 위해 정량적 평가를 진행했습니다.

  * **모델 비교:** GPT-3.5-turbo가 GPT-4 대비 속도는 **3.7배 빠르며**, 정확도는 대등함(90점)을 확인하여 채택
  * **Retriever 평가:** TF-IDF, SVM, Similarity Search 등을 비교하여 **Similarity Search** 방식 선정
  * **최종 점수 (Auto-Evaluator):**
      * **Retrieval Score:** 85점 (관련 문서 검색 정확도)
      * **Answer Score:** 83점 (답변 품질)

<br>

## 🚀 설치 및 실행 방법 (Getting Started)

### 1\. Backend (Flask)

Python 3.8+ 환경이 필요합니다.

```bash
# 레포지토리 클론
git clone https://github.com/username/InChat.git
cd InChat/server

# 의존성 설치
pip install flask langchain openai faiss-cpu tiktoken pypdf requests beautifulsoup4

# OpenAI API Key 설정 (환경 변수 또는 코드 내 설정)
export OPENAI_API_KEY='your-api-key-here'

# 서버 실행 (기본 포트 5000)
python app.py
```

### 2\. Client (Android)

1.  Android Studio에서 프로젝트 폴더를 엽니다.
2.  `KorActivity.java`, `EngActivity.java` 파일 내의 `flaskUrl` 변수를 실제 서버 IP 주소로 변경합니다.
    ```java
    // 예: String flaskUrl = "http://YOUR_SERVER_IP:5000/chat";
    ```
3.  에뮬레이터 또는 실제 기기에서 빌드 및 실행합니다.

<br>

## 📁 프로젝트 구조 (Directory Structure)

```
Capstone/
├── server/
│   ├── app.py                # Flask 서버 메인 및 RAG 로직
│   └── data/                 # 학교 홈페이지 PDF 파일들 (벡터 DB 구축용)
├── android/
│   ├── app/src/main/java/com/example/easychatgpt/
│   │   ├── MainActivity.java     # 메인 화면
│   │   ├── KorActivity.java      # 한국어 채팅 화면 (API 요청 처리)
│   │   ├── EngActivity.java      # 영어 채팅 화면
│   │   ├── Message.java          # 채팅 메시지 모델
│   │   ├── MessageAdapter.java   # 리사이클러뷰 어댑터
│   │   └── ...
│   └── res/layout/               # XML UI 레이아웃
└── README.md
```

<br>

## 👥 팀원 (Contributors)

  * **김현주 (팀장):** RAG 파이프라인 설계, Flask 서버 개발, 프롬프트 엔지니어링, 데이터셋 구축
  * **유다현:** Android 클라이언트 개발 (UI/UX, 네트워크 통신), 발표 자료 제작

-----

*Created for the 2024 Incheon National University Capstone Design Project.*
