# 유틸리티 스크립트

이 디렉토리에는 프로젝트 개발 및 운영에 사용되는 유틸리티 스크립트들이 저장되어 있습니다.

## 📁 스크립트 목록

### read_docs.py
문서 파일들을 읽고 분석하는 Python 스크립트입니다.

#### 사용 방법
```bash
python scripts/read_docs.py
```

## 🔧 스크립트 추가 가이드

새로운 스크립트를 추가할 때는 다음 가이드를 따르세요:

### 파일명 규칙
- Python: `action_target.py` (예: `export_data.py`)
- Shell: `action_target.sh` (예: `backup_db.sh`)
- 소문자와 언더스코어 사용

### 스크립트 구조
```python
#!/usr/bin/env python3
"""
스크립트명: 간단한 설명
작성일: YYYY-MM-DD
작성자: 이름
"""

def main():
    """메인 함수"""
    pass

if __name__ == "__main__":
    main()
```

### README 업데이트
새 스크립트를 추가한 후, 이 README를 업데이트하여 사용 방법을 문서화하세요.

---

**최종 수정일**: 2025-11-18
