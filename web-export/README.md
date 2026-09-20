# Project ECHO // Infrasound Sensor Array & Threat Localization

**Smart India Hackathon 2026 (SIH26144)**  
*Self-Localizing Infrasound Sensor Network & Threat Localization System*

---

## 🌐 Deploy to GitHub Pages in 2 Minutes

This repository contains both:
1. **Android App (Kotlin & Jetpack Compose)** in `/app`
2. **Interactive Web Application** in `/web-export` (Zero-dependency, standalone HTML5/Tailwind/Canvas dashboard)

### Option 1: Deploy Directly on GitHub Pages (Recommended)

1. Push this repository to your GitHub account:
   ```bash
   git init
   git add .
   git commit -m "feat: initial commit of Project ECHO web & Android app"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo-name>.git
   git push -u origin main
   ```

2. In your GitHub repository:
   - Go to **Settings** &rarr; **Pages**
   - Under **Build and deployment** &rarr; **Source**, choose **Deploy from a branch**
   - Select Branch: `main` and Folder: `/web-export` (or copy `/web-export/index.html` to the root folder `/index.html` and select `/ (root)`)
   - Click **Save**

Your live website will be accessible at:
`https://<your-username>.github.io/<your-repo-name>/`

---

### Option 2: Deploy to Vercel or Netlify (One-Click)

- **Vercel**: Import your GitHub repo, set the root directory or output directory to `web-export`, and click **Deploy**.
- **Netlify**: Drag-and-drop the `web-export` folder onto Netlify Drop or link the Git repository with publish directory set to `web-export`.

---

## 🛰️ Architecture & Features

- **Multi-Node Sensor Network**: Tri-node array (Alpha, Bravo, Charlie) configured in an equilateral triangular geometry ($\sim 15.8\text{ km}$ baseline aperture).
- **Time Difference of Arrival (TDOA)**: Millisecond-accurate cross-correlation propagation delays ($\Delta t_{AB}$, $\Delta t_{AC}$) using acoustic velocity $c \approx 343\text{ m/s}$.
- **Dual-Trace Oscilloscope**: Real-time canvas rendering raw atmospheric turbulence ($P_{atm}$) vs filtered coherent infrasound signal ($P_{signal} = P_{atm} - P_{ref}$) down to $\pm 50\text{ mPa}$.
- **Welch PSD Power Spectrum**: FFT spectrum analyzer resolving acoustic energy from $0.01\text{ Hz to } 20.0\text{ Hz}$.
- **STA/LTA Triggering Algorithm**: Adaptive energy ratio detector triggering automated alerts for blasts, rocket launches, storms, and landslides.
- **Data Export**: Export verified detection logs directly to formatted **JSON** or **CSV**.
