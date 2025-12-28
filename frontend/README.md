# Staff Alteration System - Frontend

A modern, interactive React + TypeScript frontend for managing staff timetables and schedules.

## Features

✨ **Modern Dashboard** - Beautiful, responsive UI with dark mode support
🔐 **Authentication** - Secure login/logout with JWT tokens
📅 **Timetable Management** - Create, view, and manage class schedules
👥 **Staff Assignment** - Assign staff to classes with conflict detection
📊 **Analytics** - View workload and scheduling reports
⚡ **Real-time Updates** - Instant UI updates on data changes
🎨 **Tailwind CSS** - Modern, customizable styling

## Tech Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Fast build tool
- **React Router** - Navigation
- **Tailwind CSS** - Styling
- **Zustand** - State management
- **Axios** - HTTP client
- **Lucide React** - Icons

## Installation

```bash
cd frontend
npm install
```

## Development

```bash
npm run dev
```

The app will start at `http://localhost:3000`

## Build

```bash
npm run build
```

## Project Structure

```
src/
├── api/                 # API integration
│   ├── client.ts       # Axios client setup
│   └── index.ts        # API endpoints
├── components/          # Reusable components
│   ├── common.tsx      # Common UI components
│   ├── Layout.tsx      # Main layout wrapper
│   ├── CreateTimetableModal.tsx
│   ├── TimetableTable.tsx
│   └── ProtectedRoute.tsx
├── pages/              # Page components
│   ├── LoginPage.tsx
│   ├── DashboardPage.tsx
│   ├── TimetablesPage.tsx
│   └── SettingsPage.tsx
├── store/             # State management
│   ├── authStore.ts   # Auth state
│   └── timetableStore.ts
├── App.tsx            # Main app component
├── main.tsx           # Entry point
└── index.css          # Global styles
```

## Key Features Implemented

### Authentication
- Login with credentials
- JWT token management
- Automatic token refresh
- Persistent login state

### Dashboard
- Welcome message
- Statistics cards
- Quick action buttons
- Recent activity feed
- System features overview

### Timetable Management
- Create new timetables
- View by class or staff
- Weekly schedule view
- Table format display
- Delete timetables

### Responsive Design
- Mobile-first approach
- Collapsible sidebar
- Responsive grids
- Touch-friendly buttons

## Test Credentials

```
Username: Staff1
Password: password123

Available users: Staff1, Staff2, Staff3, Staff4, Staff5
```

## API Configuration

The frontend proxies API requests to `http://localhost:8080/api`

Update the proxy in `vite.config.ts` if your backend is on a different URL.

## License

MIT
