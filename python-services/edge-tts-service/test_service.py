"""
Test script for Edge-TTS Service
Run this to test the TTS service locally
"""

import requests
import json
import sys
from pathlib import Path

# Service URL
SERVICE_URL = 'http://localhost:5000'

def test_health():
    """Test health endpoint"""
    print("\n[1] Testing Health Check...")
    try:
        response = requests.get(f'{SERVICE_URL}/health', timeout=5)
        if response.status_code == 200:
            print("✅ Health check passed")
            print(json.dumps(response.json(), indent=2))
            return True
        else:
            print(f"❌ Health check failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error connecting to service: {e}")
        print(f"   Make sure service is running: python app.py")
        return False


def test_voices():
    """Test voice list endpoint"""
    print("\n[2] Testing Voice List...")
    try:
        response = requests.get(f'{SERVICE_URL}/api/tts/voices', timeout=5)
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Found {len(data['available_voices'])} voices")
            print(f"   Default voice: {data['default_voice']}")
            for voice in data['available_voices'][:3]:
                print(f"   - {voice['code']}: {voice['name']}")
            return True
        else:
            print(f"❌ Failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False


def test_generate_mp3():
    """Test MP3 generation"""
    print("\n[3] Testing Audio Generation (MP3)...")
    try:
        payload = {
            'text': 'Welcome to English 12 Smart. This is a test audio.',
            'voice': 'en-US-AriaNeural',
            'rate': '+0%',
            'pitch': '+0Hz'
        }
        
        response = requests.post(
            f'{SERVICE_URL}/api/tts/generate',
            json=payload,
            timeout=30
        )
        
        if response.status_code == 200:
            audio_path = Path('test_audio.mp3')
            audio_path.write_bytes(response.content)
            print(f"✅ Audio generated: {len(response.content)} bytes")
            print(f"   Saved to: {audio_path.absolute()}")
            return True
        else:
            print(f"❌ Failed: {response.status_code}")
            print(response.json())
            return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False


def test_generate_base64():
    """Test base64 generation"""
    print("\n[4] Testing Audio Generation (Base64 JSON)...")
    try:
        payload = {
            'text': 'This is a test of the base64 response format.',
            'voice': 'en-US-AriaNeural'
        }
        
        response = requests.post(
            f'{SERVICE_URL}/api/tts/generate-base64',
            json=payload,
            timeout=30
        )
        
        if response.status_code == 200:
            data = response.json()
            if data['success']:
                print(f"✅ Audio generated: {data['size_bytes']} bytes")
                print(f"   Voice: {data['voice']}")
                print(f"   MIME: {data['mime_type']}")
                print(f"   Generated at: {data['generated_at']}")
                print(f"   Base64 preview: {data['audio_base64'][:50]}...")
                return True
            else:
                print(f"❌ Error: {data.get('error')}")
                return False
        else:
            print(f"❌ Failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False


def test_different_voices():
    """Test different voices"""
    print("\n[5] Testing Different Voices...")
    voices = [
        'en-US-AriaNeural',
        'en-US-BrianNeural',
        'en-GB-SoniaNeural'
    ]
    
    for voice in voices:
        try:
            payload = {
                'text': f'Hello from {voice}',
                'voice': voice
            }
            
            response = requests.post(
                f'{SERVICE_URL}/api/tts/generate',
                json=payload,
                timeout=30
            )
            
            if response.status_code == 200:
                print(f"✅ {voice}: {len(response.content)} bytes")
            else:
                print(f"❌ {voice}: {response.status_code}")
        except Exception as e:
            print(f"❌ {voice}: {e}")


def test_validation():
    """Test input validation"""
    print("\n[6] Testing Input Validation...")
    
    tests = [
        ('Empty text', {'text': '', 'voice': 'en-US-AriaNeural'}, 400),
        ('No text field', {'voice': 'en-US-AriaNeural'}, 400),
        ('Text too long', {'text': 'a' * 6000, 'voice': 'en-US-AriaNeural'}, 413),
    ]
    
    for test_name, payload, expected_code in tests:
        try:
            response = requests.post(
                f'{SERVICE_URL}/api/tts/generate',
                json=payload,
                timeout=10
            )
            
            if response.status_code == expected_code:
                print(f"✅ {test_name}: Correct error code {expected_code}")
            else:
                print(f"⚠️  {test_name}: Got {response.status_code}, expected {expected_code}")
        except Exception as e:
            print(f"❌ {test_name}: {e}")


def main():
    """Run all tests"""
    print("=" * 60)
    print("Edge-TTS Service Test Suite")
    print("=" * 60)
    print(f"Service URL: {SERVICE_URL}")
    
    results = [
        test_health(),
        test_voices(),
        test_generate_mp3(),
        test_generate_base64(),
    ]
    
    test_different_voices()
    test_validation()
    
    # Summary
    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)
    passed = sum(results)
    total = len(results)
    print(f"Passed: {passed}/{total}")
    
    if passed == total:
        print("✅ All tests passed!")
        return 0
    else:
        print("❌ Some tests failed")
        return 1


if __name__ == '__main__':
    sys.exit(main())
