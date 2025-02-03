# 🚗 SmartCharge - Intelligent Navigator for EV Charging Stations

## 📌 Overview
SmartCharge is a **mobile application** developed in **Kotlin** to enhance electric vehicle (EV) charging infrastructure in Malaysia. 
The app provides **real-time access to charging stations**, and a **home charger rental service**, aiming to eliminate range anxiety and improve user experience for EV owners.

---

## 🌟 Features
- 🔍 **Find Nearby Charging Stations**: Locate the nearest charging stations using integrated map services.
- 🏬 **Nearby Amenities**: Find cafes, malls, and other facilities while charging.
- 🔌 **Home Charger Rental**: Allows private owners to rent out their charging stations.
- 📡 **Real-time Updates**: Live data on station availability and status.
- 🔑 **Secure Authentication**: Sign in using **Firebase Authentication**.
- 📜 **Admin Controls**: Manage charging station data and reports.
- 📲 **Push Notifications**: Get alerts on charging station availability and updates.
- 🔎 **Google Search Place Integration**: Search for specific locations and navigate seamlessly.
- 🎚️ **Filter Feature**: Sort charging stations based on power levels, availability, and amenities.

---

## 🏗️ Tech Stack
### **Frontend**
- 🌐 Kotlin (Android Development)
- 📌 Google Maps API
- 📌 Google Places API
- 📌 Open Charge Map API

### **Backend & Database**
- 🔥 Firebase Authentication
- 📡 Firebase Firestore
- 🗄️ MySQL (For admin panel and data management)

### **Development Tools**
- 🛠️ Android Studio (IDE)
- 🛠️ Retrofit (API Calls)
- 🛠️ Google Firebase Services

---

## 🚀 Installation & Setup
### **Prerequisites**
- Android Studio installed
- Firebase project setup
- API Keys for Google Maps & Places

### **Clone the Repository**
```bash
 git clone https://github.com/yourusername/SmartCharge.git
 cd SmartCharge
```

### **Setup API Keys**
To enable Google Maps and Places functionalities, you must add your own API key:
1. Open `app/src/main/res/values/strings.xml` and replace `Replace your own API key` with your actual **Google Maps API Key**.
2. Open `app/google-services.json` and insert your **Google Services credentials**.

### **Run the Application**
1. Open the project in **Android Studio**.
2. Configure the `google-services.json` file from Firebase.
3. Set up API keys in `gradle.properties`.
4. Build and run the project on an **Android emulator or physical device**.

---

## 📸 Screenshots
| Login Page | Charging Map | Nearby Stations |
|------------|------------|------------|
| ![Login](https://github.com/user-attachments/assets/6302098e-2a82-4b9c-be29-f1ed0033445f) | ![Map](https://github.com/user-attachments/assets/cee98c31-3b63-43f8-9612-3b8297b55dc7) | ![Stations](https://github.com/user-attachments/assets/e4d14066-6f45-46ba-bd76-f60992a5ea96)
 |


---

## 📝 Future Enhancements
- **iOS version support**
- **Booking system for charging stations**
- **End-to-end navigation with AI suggestions**
- **Advanced filtering and personalization**

---

## 🏆 Contribution
We welcome contributions to improve **SmartCharge**. If you'd like to contribute:
1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit your changes: `git commit -m 'Add new feature'`
4. Push the changes: `git push origin feature-name`
5. Submit a pull request

---

## 📜 License
This project is **open-source** and licensed under the [MIT License](LICENSE).

---

## 📩 Contact
For any inquiries or feedback, feel free to reach out:
- **Developer:** Chew Eng Sen
- **Email:** engsen21@gmail.com
- **LinkedIn:** [Your Profile][(https://www.linkedin.com/in/eng-sen-chew-381630249/)]
- **GitHub Repository:** [SmartCharge](https://github.com/yourusername/SmartCharge)

🔋 Let's drive the future of **sustainable mobility** together! 🚀
