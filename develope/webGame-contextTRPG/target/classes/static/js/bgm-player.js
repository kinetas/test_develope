/**
 * bgm-player.js — Context TRPG BGM Player
 *
 * Responsibilities:
 *  - Track list rendering (my / public tabs)
 *  - HTML5 Audio playback with seek bar
 *  - Volume control with cyberpunk fade-in / fade-out animation
 *  - Loop toggle
 *  - BGM file upload to /api/files/upload (fileType=BGM)
 *  - Track registration via POST /api/bgm
 *  - Track deletion via DELETE /api/bgm/{id}
 */

(function () {
  'use strict';

  /* ── DOM references ─────────────────────────────────────────────────── */
  const audio             = document.getElementById('bgmAudio');
  const playPauseBtn      = document.getElementById('playPauseBtn');
  const stopBtn           = document.getElementById('stopBtn');
  const loopBtn           = document.getElementById('loopBtn');
  const seekBar           = document.getElementById('seekBar');
  const currentTimeEl     = document.getElementById('currentTime');
  const durationEl        = document.getElementById('duration');
  const volumeSlider      = document.getElementById('volumeSlider');
  const volumeIcon        = document.getElementById('volumeIcon');
  const nowPlayingName    = document.getElementById('nowPlayingName');
  const visualizer        = document.getElementById('visualizer');
  const trackListContainer = document.getElementById('trackListContainer');

  const tabMy             = document.getElementById('tabMy');
  const tabPublic         = document.getElementById('tabPublic');

  const bgmFileInput      = document.getElementById('bgmFileInput');
  const uploadBtn         = document.getElementById('uploadBtn');
  const uploadStatus      = document.getElementById('uploadStatus');
  const trackTitleInput   = document.getElementById('trackTitle');
  const isPublicCheck     = document.getElementById('isPublic');
  const addTrackBtn       = document.getElementById('addTrackBtn');
  const addTrackStatus    = document.getElementById('addTrackStatus');
  const uploadedFileIdEl  = document.getElementById('uploadedFileId');

  /* ── State ──────────────────────────────────────────────────────────── */
  let currentTab     = 'my';       // 'my' | 'public'
  let trackList      = [];         // Array<BgmTrackDTO>
  let activeTrackId  = null;
  let isLooping      = false;
  let isSeeking      = false;      // prevent seek-bar flicker during drag
  let fadeTimer      = null;

  /* ── JWT helper ─────────────────────────────────────────────────────── */
  /**
   * Reads the JWT token stored in localStorage under the key 'jwtToken'.
   * Adjust the key name if the auth flow uses a different key.
   */
  function getAuthHeaders() {
    const token = localStorage.getItem('jwtToken');
    const headers = { 'Content-Type': 'application/json' };
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }
    return headers;
  }

  function getAuthHeadersNoContentType() {
    const token = localStorage.getItem('jwtToken');
    const headers = {};
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }
    return headers;
  }

  /* ── Time formatting ─────────────────────────────────────────────────── */
  function formatTime(seconds) {
    if (isNaN(seconds) || !isFinite(seconds)) return '0:00';
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }

  /* ── Visualizer ──────────────────────────────────────────────────────── */
  function setVisualizerPlaying(playing) {
    if (playing) {
      visualizer.classList.add('visualizer--playing');
    } else {
      visualizer.classList.remove('visualizer--playing');
    }
  }

  /* ── Volume fade (cyberpunk effect) ──────────────────────────────────── */
  /**
   * Fades audio volume to `targetVol` over `durationMs` milliseconds.
   * Uses linear interpolation with ~60 fps steps.
   */
  function fadeVolume(targetVol, durationMs) {
    if (fadeTimer !== null) {
      clearInterval(fadeTimer);
      fadeTimer = null;
    }

    const startVol  = audio.volume;
    const delta     = targetVol - startVol;
    const steps     = Math.ceil(durationMs / 16);  // ~60fps
    let   step      = 0;

    if (Math.abs(delta) < 0.001) {
      audio.volume = targetVol;
      return;
    }

    fadeTimer = setInterval(() => {
      step++;
      const progress = step / steps;
      audio.volume = Math.min(1, Math.max(0, startVol + delta * progress));
      if (step >= steps) {
        clearInterval(fadeTimer);
        fadeTimer = null;
        audio.volume = targetVol;
      }
    }, 16);
  }

  /* ── Seek bar gradient refresh ───────────────────────────────────────── */
  function refreshSeekGradient(pct) {
    seekBar.style.setProperty('--seek-pct', pct + '%');
  }

  function refreshVolGradient(pct) {
    volumeSlider.style.setProperty('--vol-pct', pct + '%');
  }

  /* ── Play a track by DTO ─────────────────────────────────────────────── */
  function playTrack(track) {
    // Fade out if already playing something
    if (!audio.paused) {
      fadeVolume(0, 300);
      setTimeout(() => loadAndPlay(track), 320);
    } else {
      loadAndPlay(track);
    }
  }

  function loadAndPlay(track) {
    const targetVol = volumeSlider.value / 100;

    // Set src to file download endpoint
    audio.src = `/api/files/${track.audioFileId}`;
    audio.loop = isLooping;
    audio.volume = 0;

    activeTrackId = track.id;
    nowPlayingName.textContent = track.title;
    nowPlayingName.classList.remove('no-track');

    refreshTrackListUI();

    audio.play()
      .then(() => {
        fadeVolume(targetVol, 600);   // cyberpunk fade-in
        setVisualizerPlaying(true);
        playPauseBtn.innerHTML = '&#9646;&#9646; PAUSE';
      })
      .catch(err => {
        console.error('재생 오류:', err);
        setStatus(addTrackStatus, '재생 중 오류가 발생했습니다.', 'error');
      });
  }

  /* ── Playback event handlers ─────────────────────────────────────────── */
  audio.addEventListener('timeupdate', () => {
    if (isSeeking) return;
    const pct = audio.duration ? (audio.currentTime / audio.duration) * 100 : 0;
    seekBar.value = pct;
    refreshSeekGradient(pct);
    currentTimeEl.textContent = formatTime(audio.currentTime);
    durationEl.textContent    = formatTime(audio.duration);
  });

  audio.addEventListener('ended', () => {
    if (!isLooping) {
      setVisualizerPlaying(false);
      playPauseBtn.innerHTML = '&#9654; PLAY';
    }
  });

  audio.addEventListener('loadedmetadata', () => {
    durationEl.textContent = formatTime(audio.duration);
  });

  /* ── Controls ────────────────────────────────────────────────────────── */
  playPauseBtn.addEventListener('click', () => {
    if (!audio.src) return;

    if (audio.paused) {
      const targetVol = volumeSlider.value / 100;
      audio.volume = 0;
      audio.play().then(() => {
        fadeVolume(targetVol, 400);
        setVisualizerPlaying(true);
        playPauseBtn.innerHTML = '&#9646;&#9646; PAUSE';
      });
    } else {
      fadeVolume(0, 300);
      setTimeout(() => {
        audio.pause();
        setVisualizerPlaying(false);
        playPauseBtn.innerHTML = '&#9654; PLAY';
      }, 320);
    }
  });

  stopBtn.addEventListener('click', () => {
    fadeVolume(0, 200);
    setTimeout(() => {
      audio.pause();
      audio.currentTime = 0;
      seekBar.value = 0;
      refreshSeekGradient(0);
      currentTimeEl.textContent = '0:00';
      setVisualizerPlaying(false);
      playPauseBtn.innerHTML = '&#9654; PLAY';
    }, 220);
  });

  loopBtn.addEventListener('click', () => {
    isLooping = !isLooping;
    audio.loop = isLooping;
    if (isLooping) {
      loopBtn.innerHTML = '&#10227; LOOP: ON';
      loopBtn.classList.add('neon-btn--active');
    } else {
      loopBtn.innerHTML = '&#10227; LOOP: OFF';
      loopBtn.classList.remove('neon-btn--active');
    }
  });

  /* Seek bar */
  seekBar.addEventListener('mousedown', () => { isSeeking = true; });
  seekBar.addEventListener('touchstart', () => { isSeeking = true; });

  seekBar.addEventListener('input', () => {
    const pct = parseFloat(seekBar.value);
    refreshSeekGradient(pct);
    currentTimeEl.textContent = formatTime((pct / 100) * (audio.duration || 0));
  });

  seekBar.addEventListener('change', () => {
    if (audio.duration) {
      audio.currentTime = (seekBar.value / 100) * audio.duration;
    }
    isSeeking = false;
  });

  /* Volume slider */
  volumeSlider.addEventListener('input', () => {
    const pct = parseInt(volumeSlider.value, 10);
    audio.volume = pct / 100;
    refreshVolGradient(pct);

    if (pct === 0) {
      volumeIcon.innerHTML = '&#128264;';  // mute
    } else if (pct < 40) {
      volumeIcon.innerHTML = '&#128265;';  // low
    } else {
      volumeIcon.innerHTML = '&#128266;';  // high
    }
  });

  // Init volume gradient
  refreshVolGradient(parseInt(volumeSlider.value, 10));

  /* ── Tab switching ───────────────────────────────────────────────────── */
  tabMy.addEventListener('click', () => switchTab('my'));
  tabPublic.addEventListener('click', () => switchTab('public'));

  function switchTab(tab) {
    currentTab = tab;
    tabMy.classList.toggle('tab-btn--active', tab === 'my');
    tabPublic.classList.toggle('tab-btn--active', tab === 'public');
    loadTracks();
  }

  /* ── Fetch track list ────────────────────────────────────────────────── */
  async function loadTracks() {
    trackListContainer.innerHTML = '<div class="empty-state">로딩 중...</div>';
    try {
      const url = currentTab === 'my' ? '/api/bgm/my' : '/api/bgm/public';
      const res = await fetch(url, {
        headers: getAuthHeadersNoContentType()
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
      }

      trackList = await res.json();
      renderTrackList();
    } catch (err) {
      console.error('트랙 목록 로드 실패:', err);
      trackListContainer.innerHTML =
        '<div class="empty-state">트랙 목록을 불러올 수 없습니다.</div>';
    }
  }

  /* ── Render track list ───────────────────────────────────────────────── */
  function renderTrackList() {
    if (trackList.length === 0) {
      trackListContainer.innerHTML =
        '<div class="empty-state">등록된 트랙이 없습니다.</div>';
      return;
    }

    trackListContainer.innerHTML = '';
    trackList.forEach(track => {
      const item = createTrackItem(track);
      trackListContainer.appendChild(item);
    });
  }

  function createTrackItem(track) {
    const div = document.createElement('div');
    div.className = 'track-item' + (track.id === activeTrackId ? ' track-item--active' : '');
    div.dataset.trackId = track.id;

    const isActive = track.id === activeTrackId;
    const playIconHtml = isActive && !audio.paused
      ? '&#9646;&#9646;'
      : '&#9654;';

    div.innerHTML = `
      <div class="track-item__play-icon">${playIconHtml}</div>
      <div class="track-item__info">
        <div class="track-item__title">${escapeHtml(track.title)}</div>
        <div class="track-item__meta">
          ${escapeHtml(track.audioFileName)}
          ${track.isPublic ? ' &middot; PUBLIC' : ''}
        </div>
      </div>
      ${currentTab === 'my'
        ? `<button class="track-item__delete" data-id="${track.id}"
                   title="삭제" aria-label="트랙 삭제">&#10005;</button>`
        : ''}
    `;

    // Play on item click (excluding delete button)
    div.addEventListener('click', (e) => {
      if (e.target.closest('.track-item__delete')) return;
      playTrack(track);
    });

    // Delete button
    const delBtn = div.querySelector('.track-item__delete');
    if (delBtn) {
      delBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        deleteTrack(track.id);
      });
    }

    return div;
  }

  /* Refresh only the active states without full re-render */
  function refreshTrackListUI() {
    document.querySelectorAll('.track-item').forEach(el => {
      const id = parseInt(el.dataset.trackId, 10);
      const isActive = id === activeTrackId;
      el.classList.toggle('track-item--active', isActive);

      const icon = el.querySelector('.track-item__play-icon');
      if (icon) {
        icon.innerHTML = isActive && !audio.paused ? '&#9646;&#9646;' : '&#9654;';
      }
    });
  }

  /* ── Delete track ────────────────────────────────────────────────────── */
  async function deleteTrack(id) {
    if (!confirm('트랙을 삭제하시겠습니까?')) return;

    try {
      const res = await fetch(`/api/bgm/${id}`, {
        method: 'DELETE',
        headers: getAuthHeadersNoContentType()
      });

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
      }

      // Stop playback if deleted track was active
      if (activeTrackId === id) {
        audio.pause();
        audio.src = '';
        activeTrackId = null;
        nowPlayingName.textContent = '— 트랙을 선택하세요 —';
        nowPlayingName.classList.add('no-track');
        setVisualizerPlaying(false);
        playPauseBtn.innerHTML = '&#9654; PLAY';
      }

      loadTracks();
    } catch (err) {
      console.error('트랙 삭제 실패:', err);
      alert('트랙 삭제에 실패했습니다.');
    }
  }

  /* ── File upload ─────────────────────────────────────────────────────── */
  bgmFileInput.addEventListener('change', () => {
    const file = bgmFileInput.files[0];
    uploadStatus.textContent = file ? file.name : '파일 미선택';
    // Pre-fill track title if empty
    if (file && !trackTitleInput.value.trim()) {
      trackTitleInput.value = file.name.replace(/\.[^.]+$/, '');
    }
  });

  uploadBtn.addEventListener('click', async () => {
    const file = bgmFileInput.files[0];
    if (!file) {
      setStatus(uploadStatus, '파일을 선택하세요.', 'error');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileType', 'BGM');

    uploadBtn.disabled = true;
    setStatus(uploadStatus, '업로드 중...', 'info');

    try {
      const token = localStorage.getItem('jwtToken');
      const headers = {};
      if (token) headers['Authorization'] = 'Bearer ' + token;

      const res = await fetch('/api/files/upload', {
        method: 'POST',
        headers,
        body: formData
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || `HTTP ${res.status}`);
      }

      const data = await res.json();
      uploadedFileIdEl.value = data.fileId;
      setStatus(uploadStatus, `업로드 완료! (ID: ${data.fileId})`, 'success');
    } catch (err) {
      console.error('업로드 실패:', err);
      setStatus(uploadStatus, '업로드 실패: ' + err.message, 'error');
    } finally {
      uploadBtn.disabled = false;
    }
  });

  /* ── Add track ───────────────────────────────────────────────────────── */
  addTrackBtn.addEventListener('click', async () => {
    const title      = trackTitleInput.value.trim();
    const fileId     = uploadedFileIdEl.value;
    const pub        = isPublicCheck.checked;

    if (!title) {
      setStatus(addTrackStatus, '트랙 제목을 입력하세요.', 'error');
      return;
    }
    if (!fileId) {
      setStatus(addTrackStatus, '먼저 오디오 파일을 업로드하세요.', 'error');
      return;
    }

    addTrackBtn.disabled = true;
    setStatus(addTrackStatus, '등록 중...', 'info');

    try {
      const res = await fetch('/api/bgm', {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({
          title,
          audioFileId: parseInt(fileId, 10),
          isPublic: pub
        })
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || `HTTP ${res.status}`);
      }

      const dto = await res.json();
      setStatus(addTrackStatus, `등록 완료: "${dto.title}"`, 'success');

      // Reset form
      trackTitleInput.value      = '';
      uploadedFileIdEl.value     = '';
      bgmFileInput.value         = '';
      uploadStatus.textContent   = '파일 미선택';
      isPublicCheck.checked      = false;

      // Reload track list
      loadTracks();
    } catch (err) {
      console.error('트랙 등록 실패:', err);
      setStatus(addTrackStatus, '등록 실패: ' + err.message, 'error');
    } finally {
      addTrackBtn.disabled = false;
    }
  });

  /* ── Status helper ───────────────────────────────────────────────────── */
  function setStatus(el, msg, type) {
    const colorMap = {
      success: 'var(--neon-green)',
      error:   'var(--neon-pink)',
      info:    'var(--text-secondary)'
    };
    el.textContent = msg;
    el.style.color = colorMap[type] || colorMap.info;
  }

  /* ── HTML escape ─────────────────────────────────────────────────────── */
  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  /* ── Bootstrap ───────────────────────────────────────────────────────── */
  loadTracks();

})();
