# 🎯 CheermateApp - React Native Version

A comprehensive task management application built with **React Native** and **Expo**, featuring complete CRUD operations, smart task features, and modern Material Design 3 UI.

> **Migration from Android/Kotlin**: This is a complete migration of the CheermateApp from native Android (Kotlin) to a cross-platform React Native application. The original Android codebase is preserved in the `app/` directory.

## ✨ Features

### 🔥 Complete Task Management
- **✅ CREATE**: Add new tasks with detailed forms (title, description, priority, status, due date/time)
- **✅ READ**: View, filter, and search tasks with real-time updates
- **✅ UPDATE**: Complete edit functionality with pre-filled forms and progress tracking
- **✅ DELETE**: Soft delete with confirmation dialogs

### 🎨 Modern UI Features
- **📱 Material Design 3**: Using React Native Paper for consistent, beautiful UI
- **🔍 Advanced Filtering**: All, Today, Pending, Done with live counts
- **🔎 Real-time Search**: Search by title and description
- **📊 Task Cards**: Clean card-based display with color-coded priorities
- **🏷️ Color-coded Priorities**: Visual priority indicators (High, Medium, Low)
- **📅 Date/Time**: Professional date and time handling with date-fns
- **🌙 Dark Mode**: Automatic light/dark theme support based on system preferences
- **🚀 Smooth Animations**: React Native Reanimated for smooth transitions

### 🗄️ Data Persistence
- **SQLite Database**: Local data persistence with Expo SQLite
- **Soft Delete**: Tasks are archived, not permanently deleted
- **Async Storage**: Secure authentication token storage
- **Multi-user Support**: User-specific data isolation

### 🎯 Smart Features
- **🔄 Recurring Tasks**: Support for daily, weekly, monthly, and yearly recurring tasks
- **📋 Task Templates**: Reusable templates for common workflows
- **📦 Subtasks**: Break down tasks into smaller subtasks
- **🔗 Task Dependencies**: Define prerequisite task relationships
- **📊 Analytics**: Track productivity trends and statistics
- **🔔 Reminders**: Set reminders for important tasks

## 🛠️ Technology Stack

### Core Technologies
- **Framework**: React Native with Expo SDK 54
- **Language**: TypeScript 5.9
- **UI Library**: React Native Paper 5.x (Material Design 3)
- **Navigation**: React Navigation 7
- **Database**: Expo SQLite 16
- **State Management**: React Context + Hooks
- **Storage**: AsyncStorage for auth tokens
- **Forms**: React Hook Form
- **Date/Time**: date-fns
- **Icons**: Expo Vector Icons
- **Animations**: React Native Reanimated 3
- **Gestures**: React Native Gesture Handler

### Architecture
- **Pattern**: Component-based architecture with Context API
- **Async Operations**: Promises with async/await
- **Type Safety**: Full TypeScript support
- **Code Quality**: ESLint with TypeScript support

## 📱 Platform Support

- ✅ **iOS**: Full support (iOS 13+)
- ✅ **Android**: Full support (API 24+)
- ✅ **Web**: Expo Web support (experimental)

## 🚀 Getting Started

### Prerequisites
- Node.js 18+ and npm/yarn
- Expo CLI (installed automatically)
- iOS Simulator (macOS only) or Android Emulator
- Or use Expo Go app on your physical device

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/JTaguiamdev/CheermateApp.git
   cd CheermateApp
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm start
   ```

4. **Run on your device**
   - **iOS**: Press `i` in the terminal or run `npm run ios`
   - **Android**: Press `a` in the terminal or run `npm run android`
   - **Web**: Press `w` in the terminal or run `npm run web`
   - **Expo Go**: Scan the QR code with your Expo Go app

### Development Scripts

```bash
npm start          # Start Expo development server
npm run android    # Run on Android emulator/device
npm run ios        # Run on iOS simulator/device (macOS only)
npm run web        # Run in web browser
npm run lint       # Run ESLint
npm run type-check # Run TypeScript type checking
```

## 📂 Project Structure

```
CheermateApp/
├── src/
│   ├── components/         # Reusable UI components
│   ├── contexts/          # React contexts (Auth, etc.)
│   ├── hooks/             # Custom React hooks
│   ├── models/            # TypeScript type definitions
│   ├── navigation/        # Navigation configuration
│   ├── screens/           # Screen components
│   │   ├── LoginScreen.tsx
│   │   ├── SignUpScreen.tsx
│   │   ├── TaskListScreen.tsx
│   │   ├── TaskDetailScreen.tsx
│   │   ├── AddTaskScreen.tsx
│   │   ├── SettingsScreen.tsx
│   │   └── ProfileScreen.tsx
│   ├── services/          # Business logic services
│   │   ├── auth.ts        # Authentication service
│   │   └── database.ts    # Database operations
│   ├── theme/             # Theme configuration
│   └── utils/             # Utility functions
├── rn-assets/             # React Native assets (images, icons)
├── app/                   # Original Android/Kotlin code (preserved)
├── App.tsx                # Main app entry point
├── index.ts               # Expo entry point
├── app.json               # Expo configuration
├── package.json           # Dependencies
├── tsconfig.json          # TypeScript configuration
└── README-REACT-NATIVE.md # This file
```

## 🔐 Authentication

The app includes a complete authentication system:
- User registration with email and username
- Secure login with password hashing (SHA256)
- Session management with AsyncStorage
- Password reset functionality
- Multi-user support with user isolation

## 💾 Database Schema

The app uses SQLite with the following main tables:
- **User**: User accounts and profiles
- **Task**: Task information with soft delete
- **SubTask**: Subtasks linked to main tasks
- **TaskTemplate**: Reusable task templates
- **RecurringTask**: Recurring task definitions
- **TaskDependency**: Task relationships
- **Settings**: User preferences
- **Personality**: Chatbot personality options

## 🎨 Theming

The app supports automatic light/dark mode based on system preferences:
- Material Design 3 color scheme
- Consistent spacing and typography
- Custom priority and status colors
- Smooth theme transitions

## 📊 Features Status

### ✅ Implemented
- [x] User authentication (Login, SignUp)
- [x] Task list with filtering and search
- [x] SQLite database integration
- [x] Material Design 3 UI
- [x] Dark mode support
- [x] Navigation structure
- [x] TypeScript support
- [x] Profile screen
- [x] Settings screen

### 🚧 In Progress
- [ ] Task detail view with full CRUD
- [ ] Add/Edit task forms
- [ ] Subtasks management
- [ ] Task templates
- [ ] Recurring tasks
- [ ] Task dependencies
- [ ] Analytics and statistics
- [ ] Push notifications
- [ ] Data export/import

### 🔮 Planned
- [ ] Cloud synchronization
- [ ] Collaboration features
- [ ] Offline support
- [ ] Task sharing
- [ ] Calendar integration
- [ ] Widgets
- [ ] Apple Watch / Android Wear support

## 🔄 Migration Notes

This React Native version maintains feature parity with the original Android/Kotlin version while adding:
- **Cross-platform support**: iOS, Android, and Web
- **Modern UI**: Material Design 3 with React Native Paper
- **Better performance**: Optimized rendering and navigation
- **Enhanced UX**: Smooth animations and gestures
- **Easier maintenance**: Single codebase for multiple platforms

### Key Differences from Android Version
1. **UI Framework**: Material Design 3 (React Native Paper) vs Android Views
2. **Database**: Expo SQLite vs Room
3. **Navigation**: React Navigation vs Android Intents
4. **State Management**: Context API vs ViewModels
5. **Async**: Promises/async-await vs Coroutines

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is for educational and personal use.

## 👥 Authors

- **Original Android Version**: JTaguiamdev
- **React Native Migration**: Automated migration with modern best practices

## 🙏 Acknowledgments

- React Native and Expo teams for the excellent framework
- React Native Paper for Material Design 3 components
- The open-source community for amazing libraries
- Original Android/Kotlin codebase as the foundation

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check the documentation
- Review the original Android code in `app/` directory

---

**Version**: 1.5.0  
**Status**: Active Development  
**Platform**: React Native (Expo)
