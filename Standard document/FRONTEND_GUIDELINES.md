# FRONTEND_GUIDELINES.md

## 1. Brand Foundation

### 1.1 Document role

This document is the only authoritative front-end design system for the project.

Rules:

1. `Standard document/FRONTEND_GUIDELINES.md` is the source of truth for all future UI work.
2. Existing styles in `docs/`, legacy pages, and scattered page-level CSS are reference only.
3. When a screen conflicts with this document, this document wins.
4. Every visual decision in MVP must be traceable to a token, component rule, or layout rule in this file.

### 1.2 Design system topology

The design system uses:

1. One parent system
2. Three surface themes

Surface themes:

1. Patient App theme
2. Pharmacist Web theme
3. Doctor Web theme

This is not three independent design systems.
This is one shared brand system with role-specific surface behavior.

### 1.3 Brand direction

The official visual direction is:

`Medical trust blue + warm neutrals`

This direction must feel:

1. calm
2. credible
3. readable
4. operational
5. appropriate for elderly patient use

### 1.4 MVP visual scope

Included in MVP:

1. Light theme only
2. Full token system
3. Shared component style rules
4. Responsive rules
5. UI library usage boundaries

Not included in MVP:

1. Dark mode
2. Experimental themes
3. Role-specific full recoloring systems
4. Decorative gradients used as the main content background on every screen

### 1.5 Hard prohibitions

The following are not allowed:

1. Temporary page-level colors with no token mapping
2. Random border radius values outside the defined scale
3. New breakpoints defined inside a single page
4. A second custom red-yellow-green palette invented by a feature page
5. Mixing unrelated visual styles between patient and professional surfaces
6. Heavy glassmorphism, neon effects, strong blur, or consumer-social visual language
7. Overloaded dashboards on patient screens

## 2. Design Tokens

### 2.1 Core color palette

These values are global parent-system tokens.

| Token | Hex | Usage |
|---|---|---|
| `brand-primary` | `#2979FF` | Main brand color, primary actions, key highlights |
| `brand-secondary` | `#DBEAFF` | Secondary fill, gentle section emphasis |
| `text-primary` | `#333333` | Main text |
| `text-secondary` | `#556C8A` | Secondary text, helper text |
| `bg-primary` | `#FFFFFF` | Main background |
| `bg-secondary` | `#F5F7FA` | Secondary panel background |
| `status-success` | `#67C23A` | Generic success |
| `status-warning` | `#E6A23C` | Generic warning |
| `status-error` | `#F56C6C` | Generic error |
| `status-info` | `#909399` | Neutral status |
| `medical-high-risk` | `#FF4757` | Urgent/high risk medical status |
| `medical-medium-risk` | `#FFA502` | Pending/medium risk |
| `medical-low-risk` | `#2ED573` | Safe/complete/low risk |
| `medical-in-progress` | `#3742FA` | Processing/in review |

### 2.2 Background layering

Background layering is fixed to:

1. Primary base: `#FFFFFF`
2. Secondary layer: `#F5F7FA`
3. Accent wash: `#DBEAFF`
4. Optional weak brand gradient:
   - start: `#DBEAFF`
   - end: `#FFFFFF`

Rules:

1. Patient App may use the weak gradient for page atmosphere.
2. Professional surfaces use mostly flat light backgrounds with accent blocks.
3. Full-screen strong gradients are not allowed in professional surfaces.

### 2.3 Typography

#### Chinese body font stack

The official Chinese body font stack is:

```css
"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Noto Sans SC", sans-serif
```

#### English headline font

The official English headline font is:

`Manrope`

Fallback stack:

```css
"Manrope", "Inter", "Segoe UI", sans-serif
```

#### Font usage rules

1. Chinese paragraph and form content use the Chinese body stack.
2. English headings, numeric KPI labels, and compact dashboard headings may use `Manrope`.
3. Mixed Chinese + English UI must still prioritize Chinese readability.
4. Do not introduce new web fonts without updating this document.

### 2.4 Type scale

#### Patient App base

- Base font size: `16px`

Recommended scale:

| Token | Size |
|---|---|
| `patient-caption` | `12px` |
| `patient-helper` | `14px` |
| `patient-body` | `16px` |
| `patient-subtitle` | `18px` |
| `patient-title` | `20px` |
| `patient-page-title` | `24px` |

#### Professional surfaces base

- Base font size: `14px`

Recommended scale:

| Token | Size |
|---|---|
| `pro-caption` | `12px` |
| `pro-body` | `14px` |
| `pro-subtitle` | `16px` |
| `pro-title` | `20px` |
| `pro-page-title` | `24px` |
| `pro-metric` | `32px` |

### 2.5 Spacing scale

The spacing system uses:

1. `4px` base unit
2. `8px` primary rhythm

Official spacing tokens:

| Token | Size |
|---|---|
| `space-1` | `4px` |
| `space-2` | `8px` |
| `space-3` | `12px` |
| `space-4` | `16px` |
| `space-5` | `20px` |
| `space-6` | `24px` |
| `space-8` | `32px` |
| `space-10` | `40px` |
| `space-12` | `48px` |

Rules:

1. New layout spacing must come from this scale.
2. Arbitrary values like `13px`, `18px`, `22px`, `37px` are not allowed unless there is a documented exception.

### 2.6 Radius scale

The global radius scale is fixed:

| Token | Size |
|---|---|
| `radius-sm` | `8px` |
| `radius-md` | `12px` |
| `radius-lg` | `16px` |
| `radius-xl` | `24px` |

Usage:

1. Inputs and small pills: `8px`
2. Buttons and badges: `12px`
3. Cards and modal panels: `16px`
4. Large hero or onboarding surfaces: `24px`

### 2.7 Shadow scale

Only two shadow levels are allowed.

| Token | Value | Usage |
|---|---|---|
| `shadow-sm` | `0 2px 12px rgba(0, 0, 0, 0.10)` | Cards, light floating panels |
| `shadow-md` | `0 4px 16px rgba(0, 0, 0, 0.15)` | Dialogs, elevated action blocks |

Rules:

1. No third shadow level in MVP.
2. No hard black shadow.
3. Do not combine thick borders with heavy shadows.

### 2.8 Motion tokens

MVP motion level is:

`Light motion`

Allowed durations:

| Token | Value |
|---|---|
| `motion-fast` | `160ms` |
| `motion-base` | `240ms` |
| `motion-slow` | `320ms` |

Allowed easing:

| Token | Value |
|---|---|
| `ease-standard` | `cubic-bezier(0.2, 0, 0, 1)` |
| `ease-out` | `cubic-bezier(0, 0, 0, 1)` |

Allowed motion types:

1. Page entry
2. Card hover or press feedback
3. Toast or dialog entry
4. Reminder success feedback

Not allowed:

1. Infinite decorative animations
2. Large parallax motion
3. Bounce-heavy motion
4. Motion that slows medical task completion

### 2.9 Touch targets

Patient App minimum touch target:

`48px`

Professional surfaces minimum interactive height:

`40px`

Professional table default row height:

`44px`

## 3. Layout Rules

### 3.1 Patient App layout shell

Patient screens must prioritize:

1. one primary action per section
2. large readable text
3. clear vertical flow
4. low cognitive load

Standard patient page structure:

1. Safe top header
2. Primary task area
3. Supporting information area
4. Sticky or obvious next action

Rules:

1. Patient Home must surface today’s medication task above all secondary content.
2. Patient pages must not use dense side-by-side controls as the default layout.
3. Lists should use large tap zones and generous vertical spacing.

### 3.2 Professional layout shell

Professional surfaces use a fixed shell:

1. Left navigation
2. Top toolbar
3. Main content area

Rules:

1. Pharmacist and doctor share the same shell structure.
2. Role difference is expressed through label, route, and status emphasis, not a separate layout system.
3. The content area uses medium density.

### 3.3 Content width

Recommended content widths:

| Surface | Rule |
|---|---|
| Patient App | full width with internal padding |
| Professional dashboard | fluid width up to `1440px` practical working area |
| Professional detail form | comfortable reading width around `960px` to `1200px` |

### 3.4 Padding rules

Default page padding:

| Surface | Padding |
|---|---|
| Patient App | `16px` |
| Professional content pages | `20px` |
| Large dashboard section gaps | `24px` |

### 3.5 Grid rules

Patient App:

- vertical stack first
- two-column layout only for low-risk summary information on large screens

Professional surfaces:

- 12-column conceptual grid
- cards may use 2, 3, or 4 column patterns on wide screens
- forms should not exceed two major columns by default

## 4. Component Rules

### 4.1 Button

Variants:

1. Primary
2. Secondary
3. Ghost
4. Danger

Rules:

1. Primary button uses `#2979FF` background with white text.
2. Secondary button uses `#DBEAFF` or light neutral background with dark text.
3. Danger button uses `#F56C6C`.
4. Patient App primary action buttons must be large and obvious.
5. Professional surfaces may use a more compact height but still follow the same token mapping.

### 4.2 Input / Select / Textarea

Rules:

1. Use token-based border, radius, and text colors only.
2. Placeholder text must use secondary text color or lighter neutral.
3. Validation states must use the official status colors only.
4. Textareas in patient flows must not appear dense or admin-like.

### 4.3 Card

Rules:

1. Cards use `radius-lg` by default.
2. Cards use `shadow-sm`.
3. Patient cards may use soft gradient accents.
4. Professional cards must remain cleaner and flatter.

### 4.4 List Item

Rules:

1. Patient list items must have at least `48px` touchable rows.
2. Professional list items may be denser but must align to the spacing scale.
3. Status or action affordances must be visually aligned and not float inconsistently.

### 4.5 Status Badge

Allowed status color mapping:

| Semantic | Color |
|---|---|
| Success / completed | `#2ED573` or `#67C23A` depending on component family |
| Pending / warning | `#FFA502` or `#E6A23C` |
| Error / rejected | `#FF4757` or `#F56C6C` |
| Processing / reviewing | `#3742FA` |
| Neutral / info | `#909399` |

Rules:

1. Badge colors must come from this table.
2. No page may invent a local badge palette.

### 4.6 Toast / Alert / Dialog

Rules:

1. Toast uses light emphasis, not strong interruption.
2. Alert state color must match the official status palette.
3. Dialogs use `radius-lg` or `radius-xl` and `shadow-md`.
4. Emergency or high-risk alerts must use the medical high-risk token.

### 4.7 Table

Rules:

1. Professional tables use default row height `44px`.
2. Table header must be visually quieter than the page title.
3. Table density remains medium by default.
4. Patient App must not use desktop-style data tables for core tasks.

### 4.8 Empty State

Rules:

1. Empty states must explain what happened.
2. Empty states must provide the next action when possible.
3. Empty states may use a light accent illustration area, but not cartoon-heavy graphics.

### 4.9 Error State

Rules:

1. Error states must use the official error color.
2. Error states must explain what failed in plain language.
3. Error states must provide retry where possible.

### 4.10 Page Header

Rules:

1. Every page header contains one primary title.
2. Subtitle is optional and uses secondary text.
3. Patient page headers must stay compact and calming.
4. Professional page headers may include actions on the right.

### 4.11 Navigation

Bottom Tab rules for Patient App:

1. Use clear labels
2. Maximum of four primary tabs in MVP
3. Active tab uses brand-primary

Side Navigation rules for professional surfaces:

1. Use left navigation
2. Active state uses brand-primary accent
3. Hidden non-MVP modules must not appear for trial roles

## 5. Per-Surface Themes

### 5.1 Patient Theme

The patient surface is:

1. softer
2. larger
3. more guided
4. more touch-friendly

Patient theme rules:

1. Use Ionic token mapping for all core colors.
2. Base text size is `16px`.
3. Minimum interactive target is `48px`.
4. Soft gradient background is allowed.
5. Dense admin-like tables and control clusters are not allowed.

### 5.2 Professional Theme

The professional surface is:

1. structured
2. efficient
3. calm
4. clinically legible

Professional theme rules:

1. Use CSS variables and Element Plus theme variables as the only official color path.
2. Base text size is `14px`.
3. Default density is medium.
4. Flat or near-flat backgrounds dominate.
5. Role distinction is subtle.

### 5.3 Pharmacist Theme difference

Pharmacist theme is not a separate color system.

It differs through:

1. pharmacist labels
2. pharmacist workflow emphasis
3. action language
4. queue and processing status visibility

### 5.4 Doctor Theme difference

Doctor theme is not a separate color system.

It differs through:

1. doctor labels
2. escalated case emphasis
3. review and decision-focused flows

## 6. UI Library Policy

### 6.1 Patient App library policy

Official UI library:

`Ionic Vue 8`

Rules:

1. Patient colors must map through Ionic tokens.
2. Core primitives remain Ionic-based.
3. New patient business styles must layer on top of Ionic instead of replacing it with random standalone CSS blocks.

### 6.2 Pharmacist and Doctor library policy

Official UI library:

`Element Plus`

Exact versions:

1. Pharmacist Web: `Element Plus 2.3.7`
2. Doctor Web: `Element Plus 2.11.2`

Rules:

1. Professional colors must map through CSS variables and Element Plus theme variables.
2. Element Plus component states must be visually normalized to this document.
3. New components should prefer a shared business wrapper style instead of raw page-level overrides.

### 6.3 Shared implementation rules

1. No new component should directly hardcode an unrelated hex value in a page file.
2. New business components should expose semantic variants, not raw style props.
3. Design tokens must be defined once and reused.

## 7. Responsive Rules

### 7.1 Breakpoints

Official responsive breakpoints:

| Token | Width |
|---|---|
| `xs` | `480px` |
| `sm` | `768px` |
| `md` | `1024px` |
| `lg` | `1280px` |

### 7.2 Patient App responsiveness

Rules:

1. The patient surface is mobile-first.
2. Below `480px`, content must still remain readable with no clipped primary actions.
3. At `768px` and above, some summary cards may become two-column layouts.

### 7.3 Professional surface responsiveness

Rules:

1. Professional surfaces are desktop-first but must remain usable on small laptop widths.
2. At `1024px` and below, non-critical side content may collapse.
3. At `768px` and below, complex tables should degrade before the shell breaks.

## 8. Do / Don’t

### Do

1. Use the official tokens first.
2. Keep patient interactions large and obvious.
3. Keep professional screens structured and calm.
4. Use status colors consistently.
5. Reuse component variants instead of inventing page-specific styles.

### Don’t

1. Don’t invent a new blue.
2. Don’t invent a second warning palette.
3. Don’t use arbitrary radii.
4. Don’t add page-specific breakpoints unless this document is updated.
5. Don’t turn patient flows into admin dashboards.
6. Don’t use heavy decorative motion.
7. Don’t let Ionic and Element Plus drift into unrelated visual brands.

## 9. Component Variant Matrix

| Component | Variants required |
|---|---|
| Button | primary / secondary / ghost / danger |
| Input | default / focus / error / disabled |
| Select | default / focus / error / disabled |
| Textarea | default / focus / error / disabled |
| Card | default / emphasis / status |
| List Item | default / actionable / selected |
| Status Badge | success / warning / error / info / processing |
| Toast | success / warning / error / info |
| Dialog | default / confirmation / danger |
| Table | default / empty / loading |
| Empty State | no-data / no-plan / no-results |
| Error State | retryable / blocking |
| Page Header | patient / professional |
| Navigation | bottom-tab / side-nav / top-toolbar |

## 10. Validation checklist

This document is valid only if all of the following are true:

1. A new page can be designed without choosing new colors.
2. A new page can be designed without choosing new radii.
3. A new page can be designed without choosing new breakpoints.
4. Patient pages can be built from Ionic tokens plus business styling.
5. Professional pages can be built from Element Plus plus business styling.
6. Reviewers can reject any undefined color, shadow, radius, or breakpoint as non-compliant.
