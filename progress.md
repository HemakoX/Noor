# Patient Information / Patient Profile — Android Implementation

**Date:** 2026-08-13

---

## Objective

Implement a production-quality Patient Profile screen for the Dr Ziad Clinic Android app. The screen serves as the doctor's central workspace for viewing and editing patient data. It replaces the previous tab-based prototype with a vertically-scrollable, section-based profile designed for mobile-first medical use.

---

## UI Implemented

| Section | Status |
|---|---|
| Patient Header | ✅ Compact identity card (initials avatar, name, code, age, gender, phone) |
| Patient Information | ✅ ClinicalCard with all backend fields, Edit action |
| Medical History | ✅ Diabetes/Hypertension condition chips, HbA1c row, Edit via ModalBottomSheet |
| Baseline Eye Examination | ✅ Per-eye cards (OD/OS/OU), two-column measurement layout, previous history nested |
| Previous Eye History | ✅ Dedicated section per eye when `has_previous_history = true` |
| Injections | ✅ Vertical list with expand/collapse, drug, protocol, CMT, VA, eye tag, status badge |
| Post-Injection Information | ✅ Rendered inside injection card (green follow-up surface), all fields |
| Ocular Diagnoses | ✅ Diagnosis rows with optional eye tag, Add action |
| Edit Patient Info | ✅ FullEditScreen — prepopulated, validation (name required, age valid), isSaving state |
| Edit Medical History | ✅ ModalBottomSheet — Switch rows for boolean fields, conditional HbA1c field |
| Add Baseline | ✅ FullEditScreen — eye selector, all measurement fields, previous history toggle section |
| Add Injection | ✅ FullEditScreen — eye selector, drug/protocol dynamic pickers, date, CMT, VA |
| Add Follow-up | ✅ FullEditScreen — date, IOP, CMT, VA, lens status picker, notes |
| Add Diagnosis | ✅ ModalBottomSheet — name field, optional eye chip selector |
| Dynamic Drug/Protocol Selectors | ✅ DynamicSelectionBottomSheet — search, add new inline |
| Lens Status Picker | ✅ DynamicSelectionBottomSheet — standard ophthalmology values |
| Delete Injection | ✅ ConfirmActionDialog — "Delete Injection?" with descriptive message |
| Loading State | ✅ CircularProgressIndicator with descriptive text |
| Error State | ✅ Icon + message + Retry button |
| Empty States | ✅ Per-section (baseline, injections, diagnoses) with contextual action buttons |
| Responsive | ✅ LazyColumn fills width, no fixed widths, long names wrap, keyboard-safe with imePadding() |

---

## Architecture

```
PatientProfileScreen (Compose)
    ↓ collectAsState()
PatientProfileUiState (StateFlow)
    ↓
PatientProfileViewModel
    ↓ viewModelScope.launch
PatientRepository
    ↓ Result<T>
ApiService (Retrofit interface)
    ↓
OkHttp (AuthInterceptor → TokenAuthenticator → SessionCookieJar)
    ↓
FastAPI backend
```

Authentication is **completely untouched**. Token management, cookie jar, and interceptors are unchanged.

---

## API Endpoints

### Actually integrated (real backend calls)

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/patients/{code}/profile` | Load complete patient profile |

### Stubbed (UI-only, ready for real wiring)

| Hypothetical Method | Hypothetical Path | Purpose | Status |
|---|---|---|---|
| PUT/PATCH | `/api/v1/patients/{code}` | Update patient info | **Backend endpoint not confirmed — UI-only** |
| PUT/PATCH | `/api/v1/patients/{id}/medical-history` | Update medical history | **Backend endpoint not confirmed — UI-only** |
| POST | `/api/v1/patients/{code}/baseline` | Add baseline assessment | **Backend endpoint not confirmed — UI-only** |
| POST | `/api/v1/patients/{code}/injections` | Record injection | **Backend endpoint not confirmed — UI-only** |
| POST | `/api/v1/injections/{id}/follow-up` | Add follow-up | **Backend endpoint not confirmed — UI-only** |
| DELETE | `/api/v1/injections/{id}` | Delete injection | **Backend endpoint not confirmed — UI-only** |
| POST | `/api/v1/diagnoses` | Create/assign diagnosis | **Backend endpoint not confirmed — UI-only** |

---

## Models

### Added to PatientModels.kt

- `UpdatePatientRequest` — name, phone, age, gender
- `UpdateMedicalHistoryRequest` — diabetes, hypertension, hba1c
- `CreateBaselineRequest` — all baseline fields
- `CreateInjectionRequest` — eye, date, cmt, drug, protocol
- `CreatePostInjectionRequest` — examination_date, iop, centralMacularThickness, lens_status, notes

All models use correct `@SerialName` annotations for backend typos:
- `centeral_macular_thickness` (in PostInjectionResponse and CreatePostInjectionRequest)
- `visual_activiy` (in PostInjectionResponse)

---

## Files Created

- `progress.md` (this file)

## Files Modified

| File | Change |
|---|---|
| `PatientModels.kt` | Added 5 request model classes |
| `PatientProfileViewModel.kt` | Complete rewrite — removed hardcoded catalogs, added isSaving/saveError per section, proper draft architecture, delete confirmation flow |
| `PatientProfileScreen.kt` | Complete rewrite — full section hierarchy per spec, all edit/add/delete flows, loading/error/empty states |

---

## Backend

### Backend-connected features
- Patient profile loading — `GET /api/v1/patients/{code}/profile` — fully connected via ApiService → PatientRepository → PatientProfileViewModel

### UI-only features (no backend endpoint confirmed)
- Update patient information
- Update medical history
- Add baseline eye assessment
- Add injection record
- Add post-injection follow-up
- Delete injection
- Add ocular diagnosis

All UI-only features implement the **full draft architecture**. When the backend endpoint is available:
1. Add method to `ApiService` with correct `@HTTP` annotation and models
2. Add corresponding method to `PatientRepository` returning `Result<T>`
3. Replace the `// TODO: Wire to real endpoint` comment in `PatientProfileViewModel` with the real call
4. No UI changes required

---

## Testing

### Build
- `./gradlew assembleDebug` — **BUILD SUCCESSFUL in 44s** — 0 compilation errors, 9 tasks executed, 29 up-to-date

### Manual verification (against live backend)
- [ ] App launches
- [ ] Login succeeds
- [ ] Home screen loads
- [ ] Patient list loads
- [ ] Patient profile opens and loads data from backend
- [ ] Patient header shows correct name, code, age, gender, phone
- [ ] Patient information card shows all fields
- [ ] Medical history section shows diabetes/hypertension badges
- [ ] Baseline eye cards display OD/OS fields correctly
- [ ] Previous history section appears for eyes with history
- [ ] Injection list shows all injections (newest first)
- [ ] Expand injection to see details and post-injection
- [ ] "Add Follow-up" button appears when no post-injection exists
- [ ] Delete injection shows confirmation dialog
- [ ] Confirming delete removes from list
- [ ] Ocular diagnoses section displays assigned diagnoses
- [ ] Edit patient form opens prepopulated
- [ ] Validation blocks save on blank name or invalid age
- [ ] Medical history bottom sheet opens with correct values
- [ ] Add Baseline form submits correctly
- [ ] Add Injection with drug/protocol selection works
- [ ] Add Diagnosis bottom sheet works
- [ ] Back navigation returns to patient list
- [ ] Authentication headers intact (no tokens logged — redacted)

---

## Known Limitations

1. **No mutation endpoints confirmed** — all edit/add/delete operations are local state only. Data is lost if the app is restarted or the profile is reloaded.

2. **Drug/Protocol catalogs are session-local** — in `AddInjectionForm`, new drugs/protocols added via the bottom sheet are local to that form session only. When the backend catalog endpoints are available, these should be loaded from the API.

3. **Image display** — injection images are shown as placeholder thumbnails (icon only). Loading actual images from URLs requires adding an image-loading library (Coil, etc.) and the appropriate backend image URLs.

4. **No date picker** — date fields use plain text input with format hint (YYYY-MM-DD). A proper date picker could be added using `DatePickerDialog` from Material3.

5. **Ocular diagnoses delete** — delete action for assigned diagnoses is not implemented (no backend endpoint confirmed).
