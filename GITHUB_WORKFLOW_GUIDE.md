# 🚀 EduNet GitHub Repository Setup & Collaboration Guide

**Complete Step-by-Step Guide for Team Git Workflow**

---

## 📋 Table of Contents
1. [Initial Setup (You - Team Lead)](#initial-setup-you---team-lead)
2. [Team Members Setup](#team-members-setup)
3. [Branch Strategy](#branch-strategy)
4. [Individual Member Workflow](#individual-member-workflow)
5. [Merging Strategy](#merging-strategy)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Recommended Workflow (Better than your plan!)

### **Why This is Better:**
✅ Push working code first (establish baseline)
✅ Members work on existing structure (no confusion)
✅ Each member creates their own feature branch
✅ Use Pull Requests for review before merging
✅ Maintain working `main` branch always

### **Your Plan vs Recommended:**

| Your Plan | Recommended Plan |
|-----------|------------------|
| Empty repo → Clone → Copy files | Push complete code → Clone |
| Dev branch first | Main branch first, then dev |
| Direct commits to branches | Feature branches + Pull Requests |
| Manual merge | Pull Request reviews |

---

## 📦 Phase 1: Initial Setup (You - Team Lead)

### **Step 1: Create GitHub Repository**

1. **Go to GitHub** → Click "+" → "New repository"

2. **Repository Settings:**
   ```
   Repository name: EduNet
   Description: Educational Communication Platform - Network Programming Project
   Visibility: Public (or Private if you prefer)
   
   ❌ DO NOT initialize with:
      - README (you already have one)
      - .gitignore (we'll create custom)
      - License (add later if needed)
   ```

3. **Click "Create repository"**

### **Step 2: Prepare Local Project**

Open PowerShell in your project folder:

```powershell
cd "C:\Users\user\OneDrive\Desktop\EduNet"
```

### **Step 3: Create .gitignore File**

This is IMPORTANT to avoid pushing unnecessary files:

```powershell
# Create .gitignore
@"
# Compiled class files
bin/
*.class

# IDE files
.classpath
.project
.settings/
.vscode/
.idea/

# OS files
.DS_Store
Thumbs.db
desktop.ini

# Log files
*.log

# Backup files
*~
*.bak
*.swp

# Downloaded files (examples)
downloaded-*.txt

# Temporary files
temp/
tmp/

# Package files
*.jar
*.war
*.ear
*.zip
*.tar.gz
*.rar
"@ | Out-File -FilePath .gitignore -Encoding UTF8
```

### **Step 4: Initialize Git Repository**

```powershell
# Initialize git (if not already done)
git init

# Check git status
git status
```

### **Step 5: Stage All Files**

```powershell
# Add all files (respecting .gitignore)
git add .

# Check what will be committed
git status
```

You should see:
- ✅ All .java source files
- ✅ .bat files
- ✅ .md documentation files
- ✅ data/ folder structure
- ❌ NOT bin/ folder (compiled files)

### **Step 6: Create Initial Commit**

```powershell
# Commit with meaningful message
git commit -m "Initial commit: Complete EduNet project with all features

- Server architecture (TCP/UDP)
- Multithreading support
- Message protocol with serialization
- File transfer system
- UDP broadcasting
- GUI interfaces for Teacher/Student/Admin
- Complete documentation and guides"
```

### **Step 7: Add Remote & Push**

```powershell
# Add GitHub repository as remote
# Replace YOUR_USERNAME with your actual GitHub username
git remote add origin https://github.com/YOUR_USERNAME/EduNet.git

# Verify remote
git remote -v

# Push to main branch
git branch -M main
git push -u origin main
```

**Enter credentials when prompted:**
- Username: Your GitHub username
- Password: Your GitHub Personal Access Token (NOT your password!)

**Don't have a token?** Generate one:
1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token → Select "repo" scope → Generate
3. Copy token and use as password

### **Step 8: Verify on GitHub**

Go to your repository URL and verify all files are there!

---

## 👥 Phase 2: Add Team Members as Collaborators

### **Step 1: Add Collaborators**

1. **On GitHub**, go to your repository
2. Click **Settings** tab
3. Click **Collaborators** (left sidebar)
4. Click **Add people**
5. Enter each member's GitHub username or email
6. Click **Add [username] to this repository**

### **Step 2: Send Invitation Links**

Each member will receive an email invitation. They must:
1. Click the invitation link
2. Accept the invitation
3. Now they have push access!

---

## 🌿 Phase 3: Create Branch Structure

### **Step 1: Create Dev Branch (You)**

```powershell
# Create and switch to dev branch
git checkout -b dev

# Push dev branch to GitHub
git push -u origin dev
```

### **Step 2: Protect Main Branch (Recommended)**

On GitHub:
1. Repository → **Settings** → **Branches**
2. Under "Branch protection rules" → **Add rule**
3. Branch name pattern: `main`
4. Check:
   - ✅ Require pull request reviews before merging
   - ✅ Require status checks to pass
5. **Save changes**

Now no one can push directly to `main` - must use Pull Requests!

---

## 🔄 Phase 4: Team Members Setup

### **Each Member Does This:**

**Step 1: Clone Repository**

```powershell
# Navigate to where you want the project
cd "C:\Users\[YourName]\Documents"

# Clone the repository
git clone https://github.com/YOUR_USERNAME/EduNet.git

# Enter the folder
cd EduNet
```

**Step 2: Verify Setup**

```powershell
# Check current branch
git branch

# Should show: * main

# Check remote
git remote -v

# Should show origin pointing to GitHub
```

**Step 3: Fetch Dev Branch**

```powershell
# Get latest from remote
git fetch origin

# Switch to dev branch
git checkout dev

# Verify you're on dev
git branch
# Should show: * dev
```

**Step 4: Compile and Test**

```powershell
# Compile the project
javac -encoding UTF-8 -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java

# Test it works
java -cp bin server.ChatServer
```

If it works, you're ready to develop! ✅

---

## 🎯 Phase 5: Individual Member Workflow

### **Branch Naming Convention:**

```
feature/member1-server-architecture
feature/member2-multithreading
feature/member3-message-protocol
feature/member4-file-transfer
feature/member5-udp-broadcasting
```

### **Member 1 Workflow Example:**

**Step 1: Create Feature Branch**

```powershell
# Make sure you're on dev
git checkout dev

# Pull latest changes
git pull origin dev

# Create your feature branch
git checkout -b feature/member1-server-architecture
```

**Step 2: Make Your Changes**

Work on YOUR files only:
- `src/server/ChatServer.java`
- `src/server/ClientHandler.java`
- `docs/MEMBER_1_GUIDE.md`

**Step 3: Stage and Commit YOUR Changes**

```powershell
# Check what changed
git status

# Add ONLY your files
git add src/server/ChatServer.java
git add src/server/ClientHandler.java
git add docs/MEMBER_1_GUIDE.md

# Commit with descriptive message
git commit -m "Member 1: Enhanced server architecture

- Improved connection handling in ChatServer
- Added better error logging
- Updated documentation for TCP concepts"
```

**Step 4: Push to Your Branch**

```powershell
# Push your feature branch
git push -u origin feature/member1-server-architecture
```

**Step 5: Create Pull Request on GitHub**

1. Go to repository on GitHub
2. You'll see a banner: "Compare & pull request" → Click it
3. **Base branch**: `dev` (not main!)
4. **Compare branch**: `feature/member1-server-architecture`
5. **Title**: "Member 1: Server Architecture Enhancements"
6. **Description**:
   ```
   ## Changes Made
   - Improved connection handling in ChatServer
   - Added comprehensive error logging
   - Updated TCP documentation
   
   ## Testing
   - Tested with 5 concurrent clients
   - Server handles disconnections gracefully
   
   ## Member
   Member 1 - Server & TCP Socket Programming
   ```
7. **Reviewers**: Add team lead or other members
8. Click **Create Pull Request**

**Step 6: Wait for Review & Merge**

Team lead will:
- Review your code
- Test it
- Approve and merge to `dev`

---

## 📝 Example Workflow for All Members

### **Member 1: Server Architecture**

```powershell
git checkout dev
git pull origin dev
git checkout -b feature/member1-server-architecture

# Work on:
# - src/server/ChatServer.java
# - src/server/ClientHandler.java
# - docs/MEMBER_1_GUIDE.md

git add src/server/ChatServer.java src/server/ClientHandler.java docs/MEMBER_1_GUIDE.md
git commit -m "Member 1: Server architecture enhancements"
git push -u origin feature/member1-server-architecture

# Create PR on GitHub: feature/member1-server-architecture → dev
```

### **Member 2: Multithreading**

```powershell
git checkout dev
git pull origin dev
git checkout -b feature/member2-multithreading

# Work on:
# - src/server/ClientHandler.java (threading logic)
# - docs/MEMBER_2_GUIDE.md

git add src/server/ClientHandler.java docs/MEMBER_2_GUIDE.md
git commit -m "Member 2: Multithreading improvements"
git push -u origin feature/member2-multithreading

# Create PR on GitHub: feature/member2-multithreading → dev
```

### **Member 3: Message Protocol**

```powershell
git checkout dev
git pull origin dev
git checkout -b feature/member3-message-protocol

# Work on:
# - src/utils/Message.java
# - src/client/ChatClient.java (message handling)
# - docs/MEMBER_3_GUIDE.md

git add src/utils/Message.java src/client/ChatClient.java docs/MEMBER_3_GUIDE.md
git commit -m "Member 3: Message protocol enhancements"
git push -u origin feature/member3-message-protocol

# Create PR on GitHub: feature/member3-message-protocol → dev
```

### **Member 4: File Transfer**

```powershell
git checkout dev
git pull origin dev
git checkout -b feature/member4-file-transfer

# Work on:
# - src/server/FileTransferHandler.java
# - src/client/FileTransferClient.java
# - docs/MEMBER_4_GUIDE.md

git add src/server/FileTransferHandler.java src/client/FileTransferClient.java docs/MEMBER_4_GUIDE.md
git commit -m "Member 4: File transfer system improvements"
git push -u origin feature/member4-file-transfer

# Create PR on GitHub: feature/member4-file-transfer → dev
```

### **Member 5: UDP Broadcasting**

```powershell
git checkout dev
git pull origin dev
git checkout -b feature/member5-udp-broadcasting

# Work on:
# - src/server/UDPAnnouncementServer.java
# - src/client/UDPAnnouncementListener.java
# - docs/MEMBER_5_GUIDE.md

git add src/server/UDPAnnouncementServer.java src/client/UDPAnnouncementListener.java docs/MEMBER_5_GUIDE.md
git commit -m "Member 5: UDP broadcasting enhancements"
git push -u origin feature/member5-udp-broadcasting

# Create PR on GitHub: feature/member5-udp-broadcasting → dev
```

---

## 🔀 Phase 6: Team Lead Merging Strategy

### **Review Process:**

For each Pull Request:

1. **Review the Code**
   - Click on "Files changed" tab
   - Review each file modification
   - Add comments if needed

2. **Test Locally (Optional but Recommended)**
   ```powershell
   # Fetch the branch
   git fetch origin feature/member1-server-architecture
   
   # Check it out
   git checkout feature/member1-server-architecture
   
   # Compile and test
   javac -encoding UTF-8 -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java
   java -cp bin server.ChatServer
   ```

3. **Approve & Merge**
   - If all good, click "Approve"
   - Click "Merge pull request"
   - Choose merge method: **"Squash and merge"** (cleaner history)
   - Confirm merge
   - Delete the feature branch (GitHub will prompt)

4. **Update Local Dev**
   ```powershell
   git checkout dev
   git pull origin dev
   ```

---

## 🎯 Phase 7: Final Merge to Main

After all members' PRs are merged to `dev`:

### **Step 1: Test Dev Branch Thoroughly**

```powershell
git checkout dev
git pull origin dev

# Compile everything
javac -encoding UTF-8 -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java

# Test all features:
# - Start server
# - Connect multiple clients
# - Test chat
# - Test file transfer
# - Test UDP announcements
```

### **Step 2: Create PR from Dev to Main**

On GitHub:
1. Click "Pull requests" → "New pull request"
2. **Base**: `main`
3. **Compare**: `dev`
4. **Title**: "Release v1.0: Complete EduNet with all member contributions"
5. **Description**:
   ```
   ## Release v1.0
   
   All team members have contributed their parts:
   - ✅ Member 1: Server architecture & TCP
   - ✅ Member 2: Multithreading
   - ✅ Member 3: Message protocol
   - ✅ Member 4: File transfer
   - ✅ Member 5: UDP broadcasting
   
   ## Testing Completed
   - Server handles 5+ concurrent clients
   - Chat messaging works reliably
   - File upload/download functional
   - UDP announcements reach all clients
   
   ## Documentation
   - All member guides complete
   - Demo guide included
   - Testing guide included
   ```
6. Create PR
7. Review and merge to `main`

### **Step 3: Tag the Release**

```powershell
git checkout main
git pull origin main

# Create annotated tag
git tag -a v1.0 -m "Release v1.0: Complete EduNet Application

Team contributions:
- Server architecture & TCP
- Multithreading support
- Message protocol & serialization
- File transfer system
- UDP broadcasting
All features tested and working."

# Push tag to GitHub
git push origin v1.0
```

On GitHub, this creates a release! 🎉

---

## 📊 Visualization of Workflow

```
main branch (protected)
    ↓
    └─> dev branch
         ├─> feature/member1-server-architecture → PR → merge to dev
         ├─> feature/member2-multithreading → PR → merge to dev
         ├─> feature/member3-message-protocol → PR → merge to dev
         ├─> feature/member4-file-transfer → PR → merge to dev
         └─> feature/member5-udp-broadcasting → PR → merge to dev
    ↑
    dev → PR → merge to main (v1.0 release)
```

---

## ✅ Best Practices

### **DO:**
✅ Always pull before creating new branch
✅ Commit small, logical changes
✅ Write descriptive commit messages
✅ Test before pushing
✅ Review teammates' code
✅ Keep feature branches short-lived

### **DON'T:**
❌ Don't commit compiled files (.class files)
❌ Don't commit IDE-specific files
❌ Don't push directly to main
❌ Don't work on the same file simultaneously
❌ Don't commit large binary files
❌ Don't use vague commit messages ("fix", "update")

### **Commit Message Format:**

```
Good ✅:
"Member 1: Add connection timeout handling in ChatServer"
"Member 4: Fix buffer overflow in file transfer"
"Docs: Update MEMBER_2_GUIDE with thread pool example"

Bad ❌:
"update"
"fix bug"
"changes"
```

---

## 🛠️ Common Git Commands Reference

### **Daily Workflow:**
```powershell
# Check status
git status

# See current branch
git branch

# Switch branches
git checkout dev

# Create new branch
git checkout -b feature/my-feature

# Pull latest changes
git pull origin dev

# Add files
git add filename.java
git add .  # Add all changed files

# Commit
git commit -m "Description of changes"

# Push
git push origin feature/my-feature

# See commit history
git log --oneline

# See what changed
git diff
```

### **Syncing with Remote:**
```powershell
# Fetch all branches
git fetch origin

# Pull latest dev
git checkout dev
git pull origin dev

# Update your feature branch with latest dev
git checkout feature/my-feature
git merge dev
```

---

## 🐛 Troubleshooting

### **Problem 1: Merge Conflicts**

```powershell
# If you get merge conflict:
git pull origin dev

# Git will show conflicted files
# Open the file and look for:
<<<<<<< HEAD
Your changes
=======
Their changes
>>>>>>> dev

# Edit to resolve conflict, then:
git add conflicted-file.java
git commit -m "Resolve merge conflict in conflicted-file.java"
git push
```

### **Problem 2: Accidentally Committed to Wrong Branch**

```powershell
# If you committed to dev instead of feature branch:
git log  # Note the commit hash

# Create feature branch from current state
git checkout -b feature/my-feature

# Go back to dev and reset
git checkout dev
git reset --hard origin/dev

# Go back to feature branch (has your commit)
git checkout feature/my-feature
```

### **Problem 3: Pushed Sensitive Data**

```powershell
# Remove file from git but keep locally
git rm --cached sensitive-file.txt
git commit -m "Remove sensitive file"
git push

# Add to .gitignore
echo "sensitive-file.txt" >> .gitignore
git add .gitignore
git commit -m "Update .gitignore"
git push
```

### **Problem 4: Need to Undo Last Commit**

```powershell
# Undo last commit but keep changes
git reset --soft HEAD~1

# Undo last commit and discard changes
git reset --hard HEAD~1

# If already pushed (careful!):
git push --force origin feature/my-feature
```

---

## 📅 Recommended Timeline

### **Week 1: Setup**
- **Day 1**: Team lead creates repo and pushes initial code
- **Day 2**: Add collaborators, everyone clones repo
- **Day 3**: Everyone creates their feature branch
- **Day 4-7**: Members work on their parts

### **Week 2: Integration**
- **Day 1-3**: Members push to their branches, create PRs
- **Day 4-5**: Team lead reviews and merges to dev
- **Day 6**: Test integrated code on dev
- **Day 7**: Merge dev to main, create release

---

## 🎓 Learning Resources

### **Git Basics:**
- [GitHub Git Cheat Sheet](https://education.github.com/git-cheat-sheet-education.pdf)
- [Learn Git Branching (Interactive)](https://learngitbranching.js.org/)

### **Best Practices:**
- Write meaningful commit messages
- Commit often, push regularly
- Pull before you push
- Review code before merging

---

## 📋 Quick Setup Checklist

### **Team Lead (You):**
- [ ] Create GitHub repository
- [ ] Create .gitignore file
- [ ] Initialize git and commit all files
- [ ] Push to main branch
- [ ] Create dev branch
- [ ] Add team members as collaborators
- [ ] Protect main branch (optional but recommended)

### **Each Team Member:**
- [ ] Accept GitHub collaboration invitation
- [ ] Clone repository
- [ ] Checkout dev branch
- [ ] Create feature branch
- [ ] Work on assigned files
- [ ] Commit and push to feature branch
- [ ] Create Pull Request to dev
- [ ] Wait for review and merge

### **Final Steps (Team Lead):**
- [ ] Review all Pull Requests
- [ ] Merge all to dev branch
- [ ] Test complete application
- [ ] Create PR from dev to main
- [ ] Merge and create v1.0 release tag

---

## 🚀 Ready to Start?

Follow these steps **in order**:

1. **Right now**: Create .gitignore (see Phase 1, Step 3)
2. **Initialize git**: `git init`
3. **Add files**: `git add .`
4. **Commit**: `git commit -m "Initial commit..."`
5. **Create GitHub repo** (don't initialize with anything)
6. **Add remote**: `git remote add origin <your-repo-url>`
7. **Push**: `git push -u origin main`
8. **Create dev branch**: `git checkout -b dev; git push -u origin dev`
9. **Add collaborators** on GitHub
10. **Share repo URL** with team members

---

## 💡 Pro Tips

1. **Use GitHub Desktop** if command line is intimidating
2. **Enable notifications** for Pull Request comments
3. **Create a team Discord/Slack** for coordination
4. **Have a backup** before major operations
5. **Communicate** before working on shared files

---

**You're ready to create a professional Git workflow! 🎉**

This approach will:
- ✅ Keep your code organized
- ✅ Allow easy collaboration
- ✅ Maintain a clean history
- ✅ Enable code review
- ✅ Protect your main branch
- ✅ Make it easy to track who did what

**Good luck with your GitHub setup! 🚀**
