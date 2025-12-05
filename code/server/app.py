import os
import asyncio
from flask import Flask, request, jsonify
from langchain.chat_models import ChatOpenAI
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.vectorstores import FAISS
from langchain.document_loaders import PyPDFLoader
from langchain.prompts.chat import ChatPromptTemplate, SystemMessagePromptTemplate, HumanMessagePromptTemplate
from langchain.chains import LLMChain


import requests
from bs4 import BeautifulSoup
import datetime
import sys


app = Flask(__name__)

# 전역 변수로 데이터 캐싱
cached_pages = None
openai_model = None
db = None

async def load_and_process_data():
    global cached_pages
    if cached_pages is not None:
        return cached_pages

    cwd = os.getcwd()
    files = [f for f in os.listdir(cwd) if os.path.isfile(os.path.join(cwd, f)) and f.endswith('.pdf')]
    pages = []
    for f in files:
        loader = PyPDFLoader(os.path.join(cwd, f))
        pages.extend(loader.load_and_split())
    
    # 데이터를 캐싱
    cached_pages = pages
    return pages

async def initialize_openai_model():
    global openai_model
    if openai_model is None:
        # OpenAI 모델을 로드할 때 디바이스 설정 변경 없이 그대로 사용
        openai_model = ChatOpenAI(model_name="gpt-3.5-turbo", temperature=0.5,
                                  openai_api_key='')
    return openai_model

async def initialize_faiss_vector_store():
    global db
    if db is None:
        pages = await load_and_process_data()
        embeddings = OpenAIEmbeddings(openai_api_key='')
        db = FAISS.from_documents(pages, embeddings)
    return db

async def search_similar_pages(question):
    db = await initialize_faiss_vector_store()
    return db.similarity_search(question, k=4)

@app.route('/chat', methods=['GET', 'POST'])
@app.route('/chat', methods=['GET', 'POST'])
async def chat():
    if request.method == 'POST':
        question = request.json.get('question')

        if '학식' in question:
            try:
                html = requests.get("https://www.inu.ac.kr/inu/643/subview.do?enc=Zm5jdDF8QEB8JTJGZGluaW5nUm9vbCUyRmludSUyRnZpZXcuZG8lM0ZkYXlUeXBlJTNEdG9kYXklMjZyb29tVHlwZSUzRDElMjY%3D")
                soup = BeautifulSoup(html.text, "html.parser")

                today_menu = soup.find_all('div', {"class": "wrap-food-day"})
                menu_text = "학생식당 메뉴:\n"

                for menu in today_menu:
                    food_boxes = menu.find_all('div', {"class": "food-box"})
                    for food_box in food_boxes:
                        menu_text += food_box.get_text() + "\n"

                return jsonify({'response': menu_text })
            except Exception as e:
                return jsonify({'error': 'Failed to fetch menu: ' + str(e)}), 500
        else:
            try:
            # Initialize OpenAI model and FAISS Vector Store with timeout
                await asyncio.wait_for(
                    asyncio.gather(
                        initialize_openai_model(),
                        initialize_faiss_vector_store()
                    ),
                    timeout=10  # Adjust timeout value as needed
                )
            except asyncio.TimeoutError:
                return jsonify({'error': 'Initialization timeout'}), 500

            # Search similar pages
            retrieved_pages = await search_similar_pages(question)
            retrieved_contents = " ".join([p.page_content for p in retrieved_pages])

            system_template = """
            You are a knowledgeable and helpful assistant specializing in answering questions about Incheon National University (INU). 
            You have access to various documents about INU to provide accurate and detailed responses. 
            Use only the information from the provided documents to answer the questions. 

            Guidelines:
            1. **Accuracy**: Ensure all responses are factually correct and directly supported by the provided documents.
            2. **Brevity and Clarity**: Provide concise answers, but include enough detail to be helpful.
            3. **Language**: Respond in the language of the question. Use polite and formal language when responding in Korean, and professional tone in English.
            4. **Unavailable Information**: If the document does not contain enough information to answer a question, respond with "질문에 대한 답을 찾지 못했습니다." for Korean or "I couldn't find the answer to the question." for English.
            5. **Terminology**: Understand and use the following equivalencies:
                - '기말고사' = '기말시험' (final exam)
                - '중간고사' = '중간시험' (midterm exam)
                - '과사' = '과 사무실' (department office)
                - '전번' = '전화번호' (phone number)
                - '정보대' = '정보기술대학' (College of Information Technology)
                - '컴공' = '컴퓨터공학부' (Department of Computer Engineering)
            6. **Contextual Adaptation**: Adapt your response based on the context and details within the documents.

            Documents:
            {docs}
            """
            
            system_message_prompt = SystemMessagePromptTemplate.from_template(system_template)

            human_template = """
            Answer the following question using only the information from the provided documents: {question}
            """

            human_message_prompt = HumanMessagePromptTemplate.from_template(human_template)

            chat_prompt = ChatPromptTemplate.from_messages([system_message_prompt, human_message_prompt])
            chain = LLMChain(llm=openai_model, prompt=chat_prompt)

            response = chain.run(question=question, docs=retrieved_contents)
            response = response.replace("\n", "")

            return jsonify({'response': response})
    else:
        return jsonify({'error': 'Method Not Allowed'}), 405

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)