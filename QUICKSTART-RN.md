# 🚀 Quick Start Guide - React Native CheermateApp

## Get Started in 5 Minutes!

### Prerequisites Check
```bash
# Check if you have Node.js installed
node --version  # Should be 18+ or 20+

# Check if you have npm installed
npm --version   # Should be 8+ or 10+
```

If you don't have Node.js, download it from [nodejs.org](https://nodejs.org/)

### Step 1: Clone & Install (2 minutes)
```bash
# Clone the repository
git clone https://github.com/JTaguiamdev/CheermateApp.git
cd CheermateApp

# Install dependencies
npm install
```

### Step 2: Start the App (1 minute)
```bash
# Start the development server
npm start
```

You'll see a QR code in your terminal and a web interface will open in your browser.

### Step 3: Run on Your Device (2 minutes)

#### Option A: Use Your Phone (Easiest!)
1. **Install Expo Go app** on your phone:
   - [iOS App Store](https://apps.apple.com/app/expo-go/id982107779)
   - [Google Play Store](https://play.google.com/store/apps/details?id=host.exp.exponent)

2. **Scan the QR code** from the terminal with:
   - iOS: Camera app
   - Android: Expo Go app

3. **Wait a few seconds** and the app will load on your phone!

#### Option B: Use Emulator/Simulator
```bash
# For Android Emulator (requires Android Studio)
npm run android

# For iOS Simulator (requires macOS + Xcode)
npm run ios

# For Web Browser
npm run web
```

## 🎯 First Time User Flow

### 1. Sign Up
- Open the app and tap "Sign Up"
- Enter your details:
  - Username (required)
  - Email (required)
  - Password (6+ characters)
  - First Name (optional)
  - Last Name (optional)
- Tap "Sign Up"

### 2. Create Your First Task
- Tap the **+** button (bottom right)
- Fill in task details:
  - **Title**: "Buy groceries" (required)
  - **Description**: "Milk, eggs, bread" (optional)
  - **Category**: Select "Shopping"
  - **Priority**: Select "🟡 Medium"
  - **Status**: Keep as "Pending"
  - **Due Date**: Optional (format: YYYY-MM-DD)
  - **Progress**: Leave as 0
- Tap "Save Task"

### 3. View Your Tasks
- You'll see your task in the list
- Try the filter chips:
  - **All**: Shows all tasks
  - **Today**: Tasks due today
  - **Pending**: Incomplete tasks
  - **Done**: Completed tasks
- Use the search bar to find tasks

### 4. Manage a Task
- Tap on your task to see details
- **Add Subtasks**: Tap the + icon
  - Add "Buy milk"
  - Add "Buy eggs"
  - Add "Buy bread"
- **Check off subtasks** as you complete them
- **Mark as Done** when finished
- **Delete** if you change your mind

### 5. Explore Other Features
- **Profile Tab**: View your stats and info
- **Settings Tab**: Customize notifications and theme
- **Dark Mode**: Automatically follows your device

## 📱 Screenshots & Features

### Login & Authentication
- Beautiful Material Design 3 interface
- Secure password handling
- Form validation
- Password visibility toggle

### Task Management
- **Create Tasks**: Comprehensive form with all fields
- **View Tasks**: Card-based layout with icons
- **Filter Tasks**: Quick filters (All, Today, Pending, Done)
- **Search Tasks**: Real-time search
- **Task Details**: Full view with progress bar
- **Subtasks**: Break down complex tasks
- **Mark as Done**: One-tap completion

### User Interface
- **Material Design 3**: Modern, beautiful UI
- **Dark Mode**: Automatic theme switching
- **Smooth Animations**: Professional transitions
- **Color-Coded**: Priorities and statuses
- **Icons & Badges**: Visual indicators

## 🎨 Visual Guide

### Priority Colors
- 🔴 **High**: Red (urgent tasks)
- 🟡 **Medium**: Orange (normal tasks)
- 🟢 **Low**: Green (when you have time)

### Status Indicators
- ⏳ **Pending**: Not started yet
- 🔄 **In Progress**: Currently working on it
- ✅ **Completed**: Done!
- ❌ **Cancelled**: No longer needed
- 🔴 **Overdue**: Past due date

### Category Options
- 💼 **Work**: Professional tasks
- 🏠 **Personal**: Personal life tasks
- 🛒 **Shopping**: Shopping lists
- 📝 **Others**: Everything else

## 🛠️ Troubleshooting

### Issue: Dependencies won't install
```bash
# Clear npm cache and try again
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Issue: App won't start
```bash
# Reset the metro bundler
npm start -- --reset-cache
```

### Issue: Can't connect to device
1. Make sure your phone and computer are on the **same WiFi**
2. Try disabling firewall temporarily
3. Use tunnel mode: `npm start -- --tunnel`

### Issue: Black screen on device
1. Close the Expo Go app completely
2. Restart the development server: `npm start`
3. Scan the QR code again

## 📚 Learn More

### Development Commands
```bash
npm start          # Start development server
npm run android    # Run on Android
npm run ios        # Run on iOS (macOS only)
npm run web        # Run in browser
npm run type-check # Check TypeScript types
```

### Project Structure
```
src/
├── screens/        # All app screens
├── services/       # Business logic
├── contexts/       # State management
├── navigation/     # Navigation config
├── models/         # TypeScript types
└── theme/          # Colors & styling
```

### Key Files
- `App.tsx` - Main app entry point
- `src/services/database.ts` - All database operations
- `src/services/auth.ts` - Authentication logic
- `src/navigation/AppNavigator.tsx` - Navigation setup

## 🎓 Next Steps

1. **Explore the Code**: Browse `src/` folder
2. **Read Documentation**: Check `README-REACT-NATIVE.md`
3. **Migration Guide**: See `MIGRATION-SUMMARY.md`
4. **Customize**: Edit themes in `src/theme/theme.ts`
5. **Extend**: Add new features following existing patterns

## 🤝 Need Help?

- **Issues**: Check GitHub Issues
- **Questions**: Read the full README
- **Bugs**: Report on GitHub with details
- **Ideas**: Open a feature request

## 🎉 Enjoy!

You now have a fully functional task management app running on React Native!

Features available:
- ✅ User authentication
- ✅ Task CRUD operations
- ✅ Filtering and search
- ✅ Subtasks
- ✅ Dark mode
- ✅ Cross-platform (iOS, Android, Web)

Happy tasking! 🎯

---

**Need more details?** Check out `README-REACT-NATIVE.md` for comprehensive documentation.
