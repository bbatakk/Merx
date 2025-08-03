## Carregar Material Feature - UI Flow Visualization

### Screen 1: Material Selection
```
┌─────────────────────────────────────────┐
│ ← Carregar Material                     │
├─────────────────────────────────────────┤
│ 📋 Selecciona el material necessari     │
│ Marca els ítems que necessitaràs...     │
├─────────────────────────────────────────┤
│ ☐ Micròfon                             │
│   Shure SM58 - Micròfon dinàmic    [2] │
├─────────────────────────────────────────┤
│ ☑ Amplificador                         │
│   Yamaha MG12XU - 12 canals            │
├─────────────────────────────────────────┤
│ ☐ Cable XLR                            │
│   Mogami 2534 - Cable micròfon     [5] │
├─────────────────────────────────────────┤
│ ☑ Altaveu                              │
│   JBL EON615 - Altaveu actiu       [2] │
├─────────────────────────────────────────┤
│                                         │
│ 2 ítems seleccionats    [Continuar →]  │
└─────────────────────────────────────────┘
```

### Screen 2: Loading Checklist
```
┌─────────────────────────────────────────┐
│ ← Carregar Material                 🔄  │
├─────────────────────────────────────────┤
│ 📦 Carregant material                   │
│ 1 de 2 ítems carregats                  │
│ ████████████▒▒▒▒ 50%                   │
├─────────────────────────────────────────┤
│ ☑ Amplificador                      ✓   │
│   Yamaha MG12XU - 12 canals            │
├─────────────────────────────────────────┤
│ ☐ Altaveu                              │
│   JBL EON615 - Altaveu actiu       [2] │
├─────────────────────────────────────────┤
│                                         │
└─────────────────────────────────────────┘
```

### Screen 3: Completion State
```
┌─────────────────────────────────────────┐
│ ← Carregar Material                 🔄  │
├─────────────────────────────────────────┤
│ ✅ Càrrega completada!                  │
│ 2 de 2 ítems carregats                  │
│ ████████████████ 100%                  │
├─────────────────────────────────────────┤
│         🎉                              │
│      Ja pots marxar                     │
│ Tot el material està carregat i llest   │
│        per al bolo                      │
├─────────────────────────────────────────┤
│ ✓ Amplificador                      ✓   │
│   Yamaha MG12XU - 12 canals            │
├─────────────────────────────────────────┤
│ ✓ Altaveu                           ✓   │
│   JBL EON615 - Altaveu actiu       [2] │
└─────────────────────────────────────────┘
```

### Navigation Flow
```
Group Home Screen
        ↓
┌──────────────────┐
│ CARREGAR MATERIAL│ ← New card added
└──────────────────┘
        ↓
Selection Screen
        ↓ (Continuar)
Checklist Screen
        ↓ (All checked)
Completion Message
        ↓ (← back or 🔄 reset)
Back to Selection or Home
```

### Key UI Elements Implemented

1. **Material Selection Cards**
   - Checkbox for selection
   - Material name, brand, model
   - Description and quantity indicators
   - Visual feedback on selection

2. **Progress Tracking**
   - Linear progress bar
   - Item count display
   - Percentage completion

3. **Checklist Items**
   - Interactive checkboxes
   - Visual state changes when checked
   - Check icons for completed items

4. **Completion Celebration**
   - "Ja pots marxar" message
   - Special styling and colors
   - Celebration icons

5. **Navigation Elements**
   - Back buttons between screens
   - Reset functionality
   - Bottom action bars

All screens follow Material 3 design principles and the existing app's visual style.