"""
Edge-TTS Microservice for English 12 Smart
Generates audio from text using Microsoft Edge TTS voice
"""

import os
import asyncio
import tempfile
from io import BytesIO
from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
import edge_tts
from datetime import datetime
import logging

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# Configuration from environment or defaults
TTS_RATE = os.getenv('TTS_RATE', '+0%')  # Default: normal speed
TTS_PITCH = os.getenv('TTS_PITCH', '+0Hz')  # Default: normal pitch
DEFAULT_VOICE = os.getenv('DEFAULT_VOICE', 'en-US-AriaNeural')


class TTSError(Exception):
    """Custom exception for TTS errors"""
    pass


async def generate_audio_async(text: str, voice: str, rate: str = TTS_RATE, pitch: str = TTS_PITCH) -> bytes:
    """
    Generate audio from text using edge-tts
    
    Args:
        text: Text to convert to speech
        voice: Voice type (e.g., 'en-US-AriaNeural', 'en-GB-SoniaNeural')
        rate: Speech rate (e.g., '+50%', '-10%', '+0%')
        pitch: Speech pitch (e.g., '+10Hz', '-5Hz', '+0Hz')
    
    Returns:
        bytes: Audio data in MP3 format
    """
    try:
        if not text or len(text.strip()) == 0:
            raise TTSError("Text cannot be empty")
        
        if len(text) > 5000:
            raise TTSError(f"Text too long ({len(text)} chars). Maximum is 5000 characters.")
        
        logger.info(f"Generating audio: voice={voice}, text_len={len(text)}, rate={rate}, pitch={pitch}")
        
        # Create communicate object
        communicate = edge_tts.Communicate(text, voice, rate=rate, pitch=pitch)
        
        # Collect all audio chunks
        audio_data = BytesIO()
        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                audio_data.write(chunk["data"])
        
        audio_data.seek(0)
        logger.info(f"Audio generated successfully: {audio_data.getbuffer().nbytes} bytes")
        
        return audio_data.getvalue()
    
    except Exception as e:
        logger.error(f"Error generating audio: {str(e)}")
        raise TTSError(f"Error generating audio: {str(e)}")


def generate_audio(text: str, voice: str, rate: str = TTS_RATE, pitch: str = TTS_PITCH) -> bytes:
    """
    Synchronous wrapper for async audio generation
    """
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    try:
        return loop.run_until_complete(generate_audio_async(text, voice, rate, pitch))
    finally:
        loop.close()


# ========== API ENDPOINTS ==========

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'edge-tts-service',
        'timestamp': datetime.utcnow().isoformat()
    }), 200


@app.route('/api/tts/voices', methods=['GET'])
def list_voices():
    """
    List available voices
    Common voices:
    - en-US-AriaNeural
    - en-US-BrianNeural
    - en-GB-SoniaNeural
    - en-GB-ThomasNeural
    - en-AU-NatashaNeural
    - en-CA-ClaraNeural
    """
    voices = {
        'available_voices': [
            {'code': 'en-US-AriaNeural', 'name': 'Aria (US)', 'gender': 'Female'},
            {'code': 'en-US-BrianNeural', 'name': 'Brian (US)', 'gender': 'Male'},
            {'code': 'en-US-JennyNeural', 'name': 'Jenny (US)', 'gender': 'Female'},
            {'code': 'en-US-GuyNeural', 'name': 'Guy (US)', 'gender': 'Male'},
            {'code': 'en-GB-SoniaNeural', 'name': 'Sonia (UK)', 'gender': 'Female'},
            {'code': 'en-GB-ThomasNeural', 'name': 'Thomas (UK)', 'gender': 'Male'},
            {'code': 'en-GB-LibbyNeural', 'name': 'Libby (UK)', 'gender': 'Female'},
            {'code': 'en-AU-NatashaNeural', 'name': 'Natasha (AU)', 'gender': 'Female'},
            {'code': 'en-AU-WilliamNeural', 'name': 'William (AU)', 'gender': 'Male'},
            {'code': 'en-CA-ClaraNeural', 'name': 'Clara (CA)', 'gender': 'Female'},
            {'code': 'en-CA-LiamNeural', 'name': 'Liam (CA)', 'gender': 'Male'},
        ],
        'default_voice': DEFAULT_VOICE
    }
    return jsonify(voices), 200


@app.route('/api/tts/generate', methods=['POST'])
def generate_tts():
    """
    Generate audio from text using Edge-TTS
    
    Request JSON:
    {
        "text": "Hello, how are you?",
        "voice": "en-US-AriaNeural",
        "rate": "+0%",
        "pitch": "+0Hz"
    }
    
    Returns:
    - 200: MP3 audio file
    - 400: Bad request (missing text)
    - 413: Text too long
    - 500: Server error
    """
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No JSON data provided'}), 400
        
        text = data.get('text', '').strip()
        voice = data.get('voice', DEFAULT_VOICE)
        rate = data.get('rate', TTS_RATE)
        pitch = data.get('pitch', TTS_PITCH)
        
        if not text:
            return jsonify({'error': 'Text is required'}), 400
        
        logger.info(f"Generating TTS: voice={voice}, text_len={len(text)}")
        
        # Generate audio
        audio_bytes = generate_audio(text, voice, rate, pitch)
        
        # Return as MP3 file
        return send_file(
            BytesIO(audio_bytes),
            mimetype='audio/mpeg',
            as_attachment=True,
            download_name='audio.mp3'
        ), 200
    
    except TTSError as e:
        logger.warning(f"TTS Error: {str(e)}")
        if "too long" in str(e).lower():
            return jsonify({'error': str(e)}), 413
        return jsonify({'error': str(e)}), 400
    
    except Exception as e:
        logger.error(f"Unexpected error: {str(e)}")
        return jsonify({'error': f'Server error: {str(e)}'}), 500


@app.route('/api/tts/generate-base64', methods=['POST'])
def generate_tts_base64():
    """
    Generate audio and return as base64 string (for API consumption)
    
    Request JSON:
    {
        "text": "Hello, how are you?",
        "voice": "en-US-AriaNeural",
        "rate": "+0%",
        "pitch": "+0Hz"
    }
    
    Returns JSON:
    {
        "success": true,
        "audio_base64": "SUQz....",
        "mime_type": "audio/mpeg",
        "size_bytes": 12345,
        "generated_at": "2026-04-08T10:30:00"
    }
    """
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'success': False, 'error': 'No JSON data provided'}), 400
        
        text = data.get('text', '').strip()
        voice = data.get('voice', DEFAULT_VOICE)
        rate = data.get('rate', TTS_RATE)
        pitch = data.get('pitch', TTS_PITCH)
        
        if not text:
            return jsonify({'success': False, 'error': 'Text is required'}), 400
        
        logger.info(f"Generating base64 TTS: voice={voice}, text_len={len(text)}")
        
        # Generate audio
        audio_bytes = generate_audio(text, voice, rate, pitch)
        
        # Convert to base64
        import base64
        audio_base64 = base64.b64encode(audio_bytes).decode('utf-8')
        
        response = {
            'success': True,
            'audio_base64': audio_base64[:100] + '...' if len(audio_base64) > 100 else audio_base64,  # Preview
            'audio_full': audio_base64,  # Full base64
            'mime_type': 'audio/mpeg',
            'size_bytes': len(audio_bytes),
            'generated_at': datetime.utcnow().isoformat(),
            'voice': voice
        }
        
        return jsonify(response), 200
    
    except TTSError as e:
        logger.warning(f"TTS Error: {str(e)}")
        if "too long" in str(e).lower():
            return jsonify({'success': False, 'error': str(e)}), 413
        return jsonify({'success': False, 'error': str(e)}), 400
    
    except Exception as e:
        logger.error(f"Unexpected error: {str(e)}")
        return jsonify({'success': False, 'error': f'Server error: {str(e)}'}), 500


# ========== ERROR HANDLERS ==========

@app.errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Endpoint not found'}), 404


@app.errorhandler(500)
def server_error(error):
    return jsonify({'error': 'Internal server error'}), 500


# ========== MAIN ==========

if __name__ == '__main__':
    port = int(os.getenv('PORT', 5000))
    debug = os.getenv('FLASK_ENV') == 'development'
    
    logger.info(f"Starting Edge-TTS Service on port {port}...")
    logger.info(f"Default voice: {DEFAULT_VOICE}")
    logger.info(f"Debug mode: {debug}")
    
    app.run(
        host='0.0.0.0',
        port=port,
        debug=debug,
        use_reloader=debug
    )
