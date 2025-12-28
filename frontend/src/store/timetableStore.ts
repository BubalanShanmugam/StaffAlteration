import { create } from 'zustand'
import { TimetableTemplate } from '../api'

interface TimetableStore {
  timetables: TimetableTemplate[]
  loading: boolean
  error: string | null
  selectedClass: string
  setTimetables: (timetables: TimetableTemplate[]) => void
  addTimetable: (timetable: TimetableTemplate) => void
  updateTimetable: (id: number, timetable: TimetableTemplate) => void
  deleteTimetable: (id: number) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  setSelectedClass: (classCode: string) => void
}

export const useTimetableStore = create<TimetableStore>((set) => ({
  timetables: [],
  loading: false,
  error: null,
  selectedClass: 'CS1',
  
  setTimetables: (timetables) => set({ timetables }),
  
  addTimetable: (timetable) => set((state) => ({
    timetables: [...state.timetables, timetable],
  })),
  
  updateTimetable: (id, timetable) => set((state) => ({
    timetables: state.timetables.map((t) => (t.id === id ? timetable : t)),
  })),
  
  deleteTimetable: (id) => set((state) => ({
    timetables: state.timetables.filter((t) => t.id !== id),
  })),
  
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  setSelectedClass: (selectedClass) => set({ selectedClass }),
}))
