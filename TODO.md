# 📋 TODO List - CheermateApp

## Overview
This document tracks immediate tasks and known issues that need to be addressed in CheermateApp. For long-term planning, see [ROADMAP.md](ROADMAP.md).

**Last Updated:** December 2025  
**Current Version:** 1.5

---

## 🔴 High Priority (Critical)

### Authentication & Security
- [ ] **Test all DAO methods**
  - Verify parameter order consistency in methods
  - Test method name changes (e.g., `getById()`, `deleteById()`)
  - Ensure data integrity and no corruption
  - Write comprehensive unit tests

---

## 🟡 Medium Priority (Important)

### User Experience Features
- [ ] **Cloud Sync functionality**
  - Currently shows "🔄 Cloud sync coming soon!"
  - Location: `FragmentSettingsActivity.kt`
  - Options to consider:
    - Firebase Cloud Firestore
    - Custom REST API backend
    - Google Drive backup
    - Third-party sync service

### Task Management Enhancements
- [ ] **Task Tags/Categories**
  - Add ability to categorize tasks beyond priority
  - Use cases: Work, Personal, Shopping, Health, etc.
  - Implement tag-based filtering

- [ ] **Task Attachments**
  - Allow users to attach files/images to tasks
  - Handle file permissions
  - Implement file cleanup on task deletion

### UI/UX Improvements
- [ ] **Onboarding flow for new users**
  - Tutorial/walkthrough on first launch
  - Feature highlights
  - Personality selection guidance

---

## 🟢 Low Priority (Nice to Have)

### Statistics & Analytics
- [ ] **Enhanced statistics dashboard (UI Pending)**
  - Productivity trends over time
  - Task completion patterns
  - Time-of-day analysis
  - Weekly/monthly reports

- [ ] **Data Visualization**
  - Charts for task completion rates
  - Priority distribution pie chart
  - Progress graphs
  - Calendar heat map for completed tasks

### Collaboration Features
- [ ] **Task Sharing**
  - Share individual tasks with other users
  - Export task as text/link
  - Generate shareable task templates

- [ ] **Multi-user task assignment** (if expanding beyond single-user)
  - Assign tasks to team members
  - Track collaborative tasks
  - Comments on tasks

### Advanced Features
- [ ] **Voice Input**
  - Add tasks via voice commands
  - Voice notes for task descriptions

- [ ] **Widget Support**
  - Home screen widget showing today's tasks
  - Quick add widget
  - Progress widget

---

## 🔧 Technical Debt & Code Quality

### Testing
- [ ] **Unit tests for DAOs**
  - Test all TaskDao methods
  - Test all UserDao methods
  - Test PersonalityDao methods
  - Test SettingsDao methods

- [ ] **Integration tests**
  - Test complete user flows
  - Test data persistence across app restarts
  - Test concurrent operations

- [ ] **UI/Instrumentation tests**
  - Test all activities
  - Test navigation flows
  - Test form validations

### Documentation
- [ ] **Code documentation**
  - Add KDoc comments to all public methods
  - Document complex algorithms
  - Add usage examples

- [ ] **API documentation** (if backend is added)
  - REST API endpoints
  - Request/response formats
  - Authentication flow

### Performance
- [ ] **Database optimization**
  - Add indexes for frequently queried columns
  - Optimize slow queries
  - Database migration testing

- [ ] **Memory leak detection**
  - Profile the app for memory leaks
  - Fix any lifecycle issues
  - Optimize image/resource loading

### Code Cleanup
- [ ] **Remove unused code**
  - Clean up commented-out code
  - Remove unused imports
  - Delete unused layout files

- [ ] **Refactoring**
  - Extract repeated code into utility functions
  - Improve separation of concerns
  - Apply SOLID principles consistently

---

## 🐛 Known Issues / Bugs

### To Investigate
- [x] **Verify timestamp handling**
  - Ensure CreatedAt, UpdatedAt work correctly
  - Test across different timezones
  - Verify String timestamp conversions

- [ ] **Verify foreign key constraints**
  - Test data integrity with user deletion
  - Test personality assignment constraints
  - Test task-user relationship integrity

### UI Issues
- [ ] **Test on different screen sizes**
  - Small phones
  - Tablets
  - Foldables
  - Different orientations (portrait/landscape)

- [ ] **Accessibility improvements**
  - Add content descriptions for screen readers
  - Improve touch target sizes
  - Ensure sufficient color contrast

---

## 📦 Dependencies & Updates

- [ ] **Review and update dependencies**
  - Check for security vulnerabilities
  - Update to latest stable versions
  - Test after each major update

- [ ] **Gradle optimization**
  - Enable build cache
  - Optimize build performance
  - Update Gradle wrapper

---

## 📱 Platform & Deployment

- [ ] **Test on minimum SDK version (21)**
  - Verify compatibility
  - Test on older Android versions

- [ ] **Prepare for Play Store release**
  - Create release keystore
  - Configure ProGuard/R8 rules
  - Prepare store listing assets
  - Write app description
  - Create promotional graphics

- [ ] **CI/CD Setup**
  - Automated builds
  - Automated testing
  - Release automation

---

## 📝 Notes

### Completed Items
Track completed items here for reference:
- ✅ DAO cleanup and function consolidation (completed)
- ✅ Core CRUD operations for tasks (completed)
- ✅ User authentication (login) (completed)
- ✅ Basic dashboard/home screen (completed)
- ✅ Task filtering and search (completed)
- ✅ Settings screen structure (completed)
- ✅ Implement Forgot Password functionality (completed)
- ✅ Complete Sign Up Activity implementation (completed)
- ✅ Implement proper password hashing (completed)
- ✅ Simplified "Task created successfully" toast message for task creation dialog (completed)
- ✅ Profile Editing functionality (completed)
- ✅ Data Import feature (completed)
- ✅ SubTask functionality (completed)
- ✅ Task Reminders/Notifications (completed)
- ✅ Add actual screenshots to README (completed)
- ✅ Dark Mode support (completed)
- ✅ Recurring Tasks (completed)
- ✅ Task Templates (completed)
- ✅ Enhanced statistics dashboard (utilities implemented)
- ✅ Verify timestamp handling (completed)

### Priority Guidelines
- 🔴 **High Priority**: Security issues, critical features, blocking bugs
- 🟡 **Medium Priority**: Important features, user experience improvements
- 🟢 **Low Priority**: Nice-to-have features, optimizations, enhancements

### How to Use This Document
1. Pick tasks from high priority first
2. Mark items with `[x]` when completed
3. Add notes/comments for complex tasks
4. Move completed items to the "Completed Items" section
5. Review and update priorities regularly

---

**See also:**
- [ROADMAP.md](ROADMAP.md) - Long-term development roadmap
- [README.md](README.md) - Project overview and setup
- [CHANGELOG.md](CHANGELOG.md) - Version history and changes
- [QUICKSTART.md](QUICKSTART.md) - Quick reference guide
