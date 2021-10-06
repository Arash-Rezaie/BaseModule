# BaseModule

## tools list
- Feather: A DI platform
- Collections:
    - SegmentedList: A lazy list which enables loading big data segment by segment. It support aging mechanism to shrink list size
- SessionManager: A static memory controlled by namespace. It also does EventBus job over namespace too.
- BindProcessor (View-ViewModel glue): Binding xml to view and view to view-model is managed by this tool. It avoids memory-leak automatically.
- MEditText: EditText with ability of checking input
- Some interfaces which are not available in android 20
- PermissionActivity: A class to grant runtime permissions. Then it will notify the result
- HwInfo (Hardware Information): so limited right now
- Settings: A class to store/restore settings
- A basic file logger: Logging is done through Utils.log() method right now. It needs to be modified.
- JalaliCalendar: A tool to convert Gregorian date to Jalali date 