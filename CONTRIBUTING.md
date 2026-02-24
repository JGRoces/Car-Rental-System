# Contributing Guide — Car Rental System

This document outlines the rules and workflow for our group to follow when contributing to this project. Please read this before pushing any code.

---

## 🔀 Daily Git Workflow

Always follow this order every coding session:

1. **Pull first** before writing any code
   ```bash
   git pull origin main
   ```

2. **Code** your assigned part

3. **Commit** with a clear, short message
   ```bash
   git add .
   git commit -m "add: CustomerDAO CRUD operations"
   ```

4. **Push** your changes
   ```bash
   git push origin main
   ```

---

## ✍️ Commit Message Format

Use a simple prefix so the history is easy to read:

| Prefix | When to use |
|--------|------------|
| `add:` | Adding new files or features |
| `fix:` | Fixing a bug |
| `update:` | Modifying existing code |
| `remove:` | Deleting unused code or files |
| `docs:` | Changes to README or documentation |

### Examples
```
add: LoginGUI layout and input fields
fix: null pointer error in DatabaseConnection
update: RentalService late fee calculation
docs: update README setup instructions
```

---

## 📂 File Ownership (Assign as a Group)

To avoid merge conflicts, each member should own specific files. Discuss and fill this in together:

| File / Package | Assigned To |
|---------------|------------|
| `pckModels/` | |
| `pckDatabase/` | |
| `pckServices/` | |
| `pckAdmin/` | |
| `pckCustomer/` | |
| `pckUtils/` | |

> If you need to edit someone else's file, **communicate first** via your group chat.

---

## 🚫 What NOT to Push

- Your MySQL username and password
- IDE-specific config folders (`.idea/`, `.eclipse/`)
- Compiled output folders (`/out`, `/build`, `/target`)
- The JDBC `.jar` file (share it through the group chat instead)

These are already handled by `.gitignore`, but be mindful.

---

## ⚠️ If You Get a Merge Conflict

Don't panic. It means two people edited the same file. Here's what to do:

1. Open the conflicting file in your IDE
2. Look for the conflict markers — `<<<<<<<`, `=======`, `>>>>>>>`
3. Decide which version to keep (or combine both)
4. Save the file, then commit and push again
5. Let the group know it happened so everyone pulls the fix

---

## 💬 Communication

Always announce in the group chat:
- What file you are currently working on
- When you are about to push
- If you encounter a bug that affects shared files
