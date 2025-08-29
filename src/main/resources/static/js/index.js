function renderAssistantResponse(text) {
    // Add a subtle animation for assistant's response, and maybe highlight actionable items in the future
    return `<div class="assistant-response">${marked.parse(text)}</div>`;
}
// Advanced Text-to-Speech functionality with modern AI voice
let currentSpeech = null;
let speechQueue = [];

const apiUrl = "http://localhost:8080/api/chat";
const conversationId = generateUuid();

const log = document.getElementById("log");
const input = document.getElementById("userInput");
const btn = document.getElementById("sendBtn");
const chatForm = document.getElementById("chatForm");
const chatStatus = document.getElementById("chatStatus");

// --- Voice Only Mode UI Elements ---
let voiceMode = false;
let micBtn = null;
let voiceModeBtn = null;
let recognition = null;
let recognizing = false;
let lastTranscript = "";

function setupVoiceUI() {
    // Wire up the Voice Only button in the input bar to enter voice mode
    const voiceModeBtn = document.getElementById("voiceModeBtn");
    if (voiceModeBtn) {
        voiceModeBtn.onclick = () => enterVoiceMode();
    }
    // Remove any extra voiceModeBtn if present above the container (legacy)
    const allVoiceBtns = document.querySelectorAll("#voiceModeBtn");
    if (allVoiceBtns.length > 1) {
        for (let i = 0; i < allVoiceBtns.length - 1; i++) {
            allVoiceBtns[i].parentNode.removeChild(allVoiceBtns[i]);
        }
    }
    // Remove legacy micBtn if present
    const micBtn = document.getElementById("micBtn");
    if (micBtn) micBtn.remove();
    // Wire up exit button for voice mode
    const exitVoiceModeBtn = document.getElementById("exitVoiceModeBtn");
    if (exitVoiceModeBtn) {
        exitVoiceModeBtn.onclick = () => exitVoiceMode();
    }
}

function enterVoiceMode() {
    voiceMode = true;
    document.getElementById("chatForm").style.display = "none";
    document.getElementById("voiceOnlyBar").style.display = "";
    setStatus("Voice Only Mode: Listening…");
    startContinuousVoiceInput();
}

function exitVoiceMode() {
    voiceMode = false;
    document.getElementById("chatForm").style.display = "";
    document.getElementById("voiceOnlyBar").style.display = "none";
    setStatus("Ready to help");
    stopRecognition();
}

function startContinuousVoiceInput() {
    if (!("webkitSpeechRecognition" in window) && !("SpeechRecognition" in window)) {
        setVoiceStatus("Speech Recognition not supported in this browser.", true);
        return;
    }
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!recognition) {
        recognition = new SpeechRecognition();
        recognition.lang = "en-US";
        recognition.interimResults = false;
        recognition.maxAlternatives = 1;
        recognition.continuous = false;
    }
    recognition.onstart = () => {
        recognizing = true;
        setVoiceStatus("Listening…");
        const micIndicator = document.getElementById("micIndicator");
        if (micIndicator) micIndicator.style.color = "#7fffd4";
    };
    recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        lastTranscript = transcript;
        recognizing = false;
        setVoiceStatus("Processing…");
        appendLog("user", transcript, true);
        sendMessage(transcript);
    };
    recognition.onerror = (event) => {
        recognizing = false;
        setVoiceStatus("Error: " + event.error, true);
        setTimeout(() => {
            if (voiceMode) startContinuousVoiceInput();
        }, 1200);
    };
    recognition.onend = () => {
        recognizing = false;
        if (voiceMode) {
            setTimeout(() => startContinuousVoiceInput(), 400);
        }
    };
    if (recognition && !recognizing) {
        recognition.start();
    }
}

function setVoiceStatus(msg, error = false) {
    const voiceStatus = document.getElementById("voiceStatus");
    const micIndicator = document.getElementById("micIndicator");
    if (voiceStatus) {
        voiceStatus.textContent = msg;
        voiceStatus.style.color = error ? "#ff6b6b" : "#7fffd4";
    }
    if (micIndicator) {
        micIndicator.style.color = error ? "#ff6b6b" : "#7fffd4";
    }
}

function startVoiceInput() {
    if (!("webkitSpeechRecognition" in window) && !("SpeechRecognition" in window)) {
        alert("Speech Recognition not supported in this browser.");
        return;
    }
    if (recognizing) {
        stopRecognition();
        return;
    }
    // Prefer standard API, fallback to webkit
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!recognition) {
        recognition = new SpeechRecognition();
        recognition.lang = "en-US";
        recognition.interimResults = false;
        recognition.maxAlternatives = 1;
        recognition.continuous = false;
        recognition.onstart = () => {
            recognizing = true;
            micBtn.textContent = "🛑 Stop";
            setStatus("Listening...");
        };
        recognition.onresult = (event) => {
            const transcript = event.results[0][0].transcript;
            lastTranscript = transcript;
            recognizing = false;
            micBtn.textContent = "🎙️ Speak";
            setStatus("Processing...");
            appendLog("user", transcript, true);
            sendMessage(transcript);
        };
        recognition.onerror = (event) => {
            recognizing = false;
            micBtn.textContent = "🎙️ Speak";
            setStatus("Error: " + event.error);
        };
        recognition.onend = () => {
            recognizing = false;
            micBtn.textContent = "🎙️ Speak";
            if (!lastTranscript) setStatus("Voice Only Mode: Tap mic and speak");
        };
    }
    if (recognition && !recognizing) {
        recognition.start();
    }
}

function stopRecognition() {
    if (recognition && recognizing) {
        recognition.stop();
    }
}

// Focus input on load

window.onload = () => {
    input.focus();
    setupVoiceUI();
};

chatForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const msg = input.value.trim();
    if (!msg) return;
    appendLog("user", msg, true);
    input.value = "";
    sendMessage(msg);
});

async function sendMessage(message) {
    // Show AI is typing...
    setStatus("Phantom is typing...");
    appendLog("assistant-typing", "...");
    try {
        const res = await fetch(apiUrl, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message, conversationId }),
        });
        if (!res.ok) throw new Error(res.statusText);
        const { response, emotion } = await res.json();

        removeTyping();
        appendLog("assistant", response, false, emotion);
        // In voice mode, speak the response and allow interruption, update status accordingly
        if (voiceMode) {
            speakTextWithInterruption(response, emotion, () => {
                if (voiceMode) startContinuousVoiceInput();
            });
        } else {
            speakText(response, emotion);
        }
        setStatus("Ready to help");

// Speak text, but if user starts talking, interrupt and make it listening, then resume listening after speaking
function speakTextWithInterruption(text, emotion = "neutral", onDone) {
    // Stop any ongoing speech
    if (speechSynthesis.speaking) {
        speechSynthesis.cancel();
    }
    let interrupted = false;
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    let interruptRecognition = null;
    // Set status to speaking
    setVoiceStatus("Speaking…", false);
    if (SpeechRecognition) {
        interruptRecognition = new SpeechRecognition();
        interruptRecognition.lang = "en-US";
        interruptRecognition.interimResults = false;
        interruptRecognition.continuous = true;
        interruptRecognition.onstart = () => {
            // Optionally update UI to show "Listening for interruption..."
        };
        interruptRecognition.onresult = (event) => {
            if (!interrupted) {
                interrupted = true;
                speechSynthesis.cancel();
                interruptRecognition.stop();
                setVoiceStatus("Listening…", false);
                if (typeof onDone === "function") setTimeout(onDone, 300);
            }
        };
        interruptRecognition.onerror = () => {};
        interruptRecognition.onend = () => {};
        interruptRecognition.start();
    }
    // Speak the text
    const utterance = new SpeechSynthesisUtterance(cleanTextForSpeech(text));
    configureModernVoice(utterance, emotion);
    utterance.onstart = () => {
        setVoiceStatus("Speaking…", false);
    };
    utterance.onend = () => {
        if (interruptRecognition && !interrupted) {
            interruptRecognition.stop();
        }
        if (!interrupted && typeof onDone === "function") {
            setVoiceStatus("Listening…", false);
            setTimeout(onDone, 200);
        }
    };
    utterance.onerror = () => {
        if (interruptRecognition) interruptRecognition.stop();
        if (typeof onDone === "function") setTimeout(onDone, 200);
    };
    try {
        speechSynthesis.speak(utterance);
    } catch (error) {
        if (interruptRecognition) interruptRecognition.stop();
        if (typeof onDone === "function") setTimeout(onDone, 200);
    }
}
    } catch (err) {
        removeTyping();
        appendLog("error", err.message);
        setStatus("Error");
    }
}

function appendLog(who, text, isUser = false, emotion = "neutral") {
    const entry = document.createElement("div");
    let html = "";
    if (who === "user") {
        html = `<div class="chat-bubble user personal-user"><span class="bubble-meta">You</span>${escapeHTML(
            text
        )}</div>`;
    } else if (who === "assistant") {
        html = `<div class="chat-bubble assistant personal-assistant" data-emotion="${emotion}"><span class="bubble-meta">Phantom Assistant</span>${renderAssistantResponse(
            text
        )}</div>`;
    } else if (who === "assistant-typing") {
        entry.className = "assistant-typing-bubble";
        html = `<div class="chat-bubble assistant personal-assistant"><span class="bubble-meta">Phantom Assistant</span><span class="typing-dots"><span>.</span><span>.</span><span>.</span></span></div>`;
    } else {
        html = `<div class="chat-bubble error"><span class="bubble-meta">${who}</span>${escapeHTML(text)}</div>`;
    }
    entry.innerHTML = html;
    log.appendChild(entry);
    log.scrollTop = log.scrollHeight;
}

function removeTyping() {
    const typing = log.querySelector(".assistant-typing-bubble");
    if (typing) log.removeChild(typing);
}

function setStatus(status) {
    if (chatStatus) chatStatus.textContent = status;
    if (status === "Ready to help") {
        chatStatus.style.color = "#7fffd4";
    } else if (status === "Error") {
        chatStatus.style.color = "#ff6b6b";
    } else {
        chatStatus.style.color = "#7fffd4";
    }
}

function escapeHTML(str) {
    return str.replace(/[&<>"']/g, function (tag) {
        const charsToReplace = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;",
        };
        return charsToReplace[tag] || tag;
    });
}

function generateUuid() {
    let d = new Date().getTime();
    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
        const r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === "x" ? r : (r & 0x3) | 0x8).toString(16);
    });
}

function speakText(textToSpeak, emotion = "neutral") {
    textToSpeak = cleanTextForSpeech(textToSpeak);

    // Create speech synthesis utterance
    const utterance = new SpeechSynthesisUtterance(textToSpeak);

    // Configure for modern AI-style speech
    configureModernVoice(utterance, emotion);

    // Speech event handlers
    utterance.onstart = () => {
        currentSpeech = utterance;
        console.log("Speech started");
    };

    utterance.onend = () => {
        currentSpeech = null;
        console.log("Speech ended");
    };

    utterance.onerror = (event) => {
        console.error("Speech error:", event.error);
        currentSpeech = null;

        // Show user-friendly error message
        if (event.error === "network") {
            alert("Network error occurred during speech synthesis. Please check your internet connection.");
        } else if (event.error === "synthesis-failed") {
            alert("Speech synthesis failed. Please try again.");
        } else {
            alert("An error occurred during speech synthesis: " + event.error);
        }
    };

    utterance.onpause = () => {
        console.log("Speech paused");
    };

    utterance.onresume = () => {
        console.log("Speech resumed");
    };

    // Speak the text
    try {
        speechSynthesis.speak(utterance);
    } catch (error) {
        console.error("Error starting speech:", error);
        alert("Failed to start speech synthesis. Please try again.");
    }
}

// Configure modern AI-style voice settings
function configureModernVoice(utterance, emotion = "neutral") {
    // Get available voices
    const voices = speechSynthesis.getVoices();

    // --- Use selected voice from UI if set ---
    let selectedVoice = null;
    if (typeof window.__selectedVoiceName === "function") {
        const name = window.__selectedVoiceName();
        if (name) {
            selectedVoice = voices.find((v) => v.name === name);
        }
    }

    // Enhanced voice selection for more natural sound
    if (!selectedVoice) {
        const naturalVoices = [
            "Google UK English",
            // 'Google US English'
        ];

        // First, try to find premium natural voices
        for (const naturalVoice of naturalVoices) {
            const found = voices.find(
                (voice) =>
                    voice.name &&
                    (voice.name.includes(naturalVoice) || voice.name.toLowerCase().includes(naturalVoice.toLowerCase()))
            );
            if (found) {
                selectedVoice = found;
                console.log("Found premium natural voice:", selectedVoice.name);
                break;
            }
        }
    }

    // If no premium voice found, look for any high-quality English voice
    if (!selectedVoice) {
        // Filter voices by quality indicators
        const qualityVoices = voices.filter((voice) => {
            if (!voice.name || !voice.lang) return false;
            const name = voice.name.toLowerCase();
            const lang = voice.lang.toLowerCase();
            // Prioritize voices with quality indicators
            return (
                (lang.includes("en-") || lang.includes("en_")) &&
                (name.includes("natural") ||
                    name.includes("neural") ||
                    name.includes("premium") ||
                    name.includes("enhanced") ||
                    name.includes("google") ||
                    name.includes("microsoft")) &&
                !name.includes("espeak")
            ); // Avoid low-quality espeak voices
        });

        // Select the best quality voice
        selectedVoice =
            qualityVoices[0] ||
            voices.find((voice) => voice.lang && (voice.lang.startsWith("en-") || voice.lang.includes("en")));
    }

    // Apply the selected voice
    if (selectedVoice) {
        utterance.voice = selectedVoice;
        console.log("Selected voice:", selectedVoice.name, selectedVoice.lang);
    }

    // Enhanced natural speech characteristics
    const baseRate = 0.85; // Slower base rate for more natural delivery
    const basePitch = 0.95; // Slightly lower pitch for less robotic sound
    const baseVolume = 0.9; // Higher volume for clarity

    // More sophisticated variations for natural speech patterns
    const naturalVariations = {
        neutral: { rate: baseRate + 0.05, pitch: basePitch + 0.05, emphasis: "conversational" },
        thoughtful: { rate: baseRate - 0.05, pitch: basePitch + 0.03, emphasis: "thoughtful" },
        energetic: { rate: baseRate + 0.1, pitch: basePitch + 0.12, emphasis: "energetic" },
        calm: { rate: baseRate - 0.1, pitch: basePitch - 0.02, emphasis: "calm" },
        friendly: { rate: baseRate + 0.02, pitch: basePitch + 0.05, emphasis: "friendly" },
        warm: { rate: baseRate - 0.03, pitch: basePitch + 0.07, emphasis: "warm" },
    };

    const variation = naturalVariations[emotion] || naturalVariations["neutral"];

    utterance.rate = Math.max(0.7, Math.min(1.2, variation.rate)); // Clamp between 0.7-1.2
    utterance.pitch = Math.max(0.8, Math.min(1.3, variation.pitch)); // Clamp between 0.8-1.3
    utterance.volume = baseVolume;

    console.log(
        `Voice configured with ${variation.emphasis} style: rate=${utterance.rate.toFixed(
            2
        )}, pitch=${utterance.pitch.toFixed(2)}`
    );
}

// Clean text for better speech synthesis
function cleanTextForSpeech(text) {
    return (
        text
            // Remove HTML tags if any
            .replace(/<[^>]*>/g, "")
            // Replace emojis with descriptive text and add natural pauses
            .replace(/🔥/g, ", fire, ")
            .replace(/💀/g, ", skull, ")
            .replace(/🤣/g, ", laughing, ")
            .replace(/😈/g, ", devil, ")
            .replace(/🤯/g, ", mind blown, ")
            .replace(/😱/g, ", shocked, ")
            .replace(/🤬/g, ", angry, ")
            .replace(/💩/g, ", poop, ")
            .replace(/👻/g, ", ghost, ")
            .replace(/😜/g, ", winking, ")
            .replace(/🎯/g, ", target, ")
            .replace(/⚡/g, ", lightning, ")
            .replace(/💥/g, ", explosion, ")
            // Replace common internet slang for better pronunciation
            .replace(/\blol\b/gi, "laugh out loud")
            .replace(/\blomg\b/gi, "oh my god")
            .replace(/\bwtf\b/gi, "what the heck")
            .replace(/\btbh\b/gi, "to be honest")
            .replace(/\bimo\b/gi, "in my opinion")
            .replace(/\bfyi\b/gi, "for your information")
            .replace(/\bbtw\b/gi, "by the way")
            .replace(/\bsmh\b/gi, "shaking my head")
            .replace(/\brofl\b/gi, "rolling on floor laughing")
            .replace(/\blmao\b/gi, "laughing my butt off")
            // Fix common pronunciation issues
            .replace(/\bu\b/gi, "you")
            .replace(/\bur\b/gi, "your")
            .replace(/\br\b/gi, "are")
            .replace(/\bn\b/gi, "and")
            .replace(/\bw\/\b/gi, "with")
            .replace(/\bw\b/gi, "with")
            .replace(/\bc\b/gi, "see")
            // Handle contractions more naturally
            .replace(/won't/gi, "will not")
            .replace(/can't/gi, "cannot")
            .replace(/shouldn't/gi, "should not")
            .replace(/wouldn't/gi, "would not")
            .replace(/couldn't/gi, "could not")
            // Add natural breathing pauses for longer sentences
            .replace(/(\w+),\s*(\w+)/g, "$1, $2") // Add space after commas
            .replace(/(\w+)\.\s*(\w+)/g, "$1. $2") // Add space after periods
            .replace(/(\w+)!\s*(\w+)/g, "$1! $2") // Add space after exclamations
            .replace(/(\w+)\?\s*(\w+)/g, "$1? $2") // Add space after questions
            // Add emphasis pauses around key words
            .replace(/\b(but|however|although|though|yet|still)\b/gi, ", $1, ")
            .replace(/\b(because|since|as|so|therefore|thus)\b/gi, ", $1 ")
            .replace(/\b(and|or|nor)\b/gi, " $1 ")
            // Remove excessive punctuation but keep natural flow
            .replace(/[!]{2,}/g, "!")
            .replace(/[?]{2,}/g, "?")
            .replace(/[.]{3,}/g, "... ")
            // Add strategic pauses for dramatic effect
            .replace(/([.!?])\s*([A-Z])/g, "$1 $2") // Pause between sentences
            .replace(/,\s*/g, ", ") // Consistent comma spacing
            // Clean up multiple spaces but preserve intentional pauses
            .replace(/\s{3,}/g, "  ") // Max 2 spaces for pauses
            .replace(/\s+/g, " ") // Single spaces elsewhere
            .trim()
    );
}

// Initialize voices when they become available
function initializeVoices() {
    if (speechSynthesis.getVoices().length === 0) {
        // Voices not loaded yet, wait for them
        speechSynthesis.addEventListener("voiceschanged", () => {
            console.log(speechSynthesis.getVoices());
            console.log("Voices loaded:", speechSynthesis.getVoices().length);
        });
    }
}

// Initialize when page loads
document.addEventListener("DOMContentLoaded", () => {
    initializeVoices();
    // Make window larger for a more immersive assistant view
    if (window.resizeTo) {
        window.resizeTo(700, 800);
    }
});

// Stop all speech when page is about to unload
window.addEventListener("beforeunload", () => {
    if (speechSynthesis.speaking) {
        speechSynthesis.cancel();
    }
});
