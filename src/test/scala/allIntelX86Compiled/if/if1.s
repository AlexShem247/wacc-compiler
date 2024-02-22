.intel_syntax noprefix
.globl main
.section .rodata
.text
main:
 push rbp
 # push {rbx, r12}
 sub rsp, 16
 mov qword ptr [rsp], rbx
 mov qword ptr [rsp + 8], r12
 mov rbp, rsp
 # Stack pointer unchanged, no stack allocated variables
 # Stack pointer unchanged, no stack allocated arguments
 mov rax, 4
 mov rdi, rax
 mov rax, 8
 mov rsi, rax
 call wacc_f
 mov r11, rax
 # Stack pointer unchanged, no stack allocated arguments
 mov rax, r11
 mov r12, rax
 # Stack pointer unchanged, no stack allocated variables
 mov rax, 0
 # pop {rbx, r12}
 mov rbx, qword ptr [rsp]
 mov r12, qword ptr [rsp + 8]
 add rsp, 16
 pop rbp
 ret

wacc_f:
 push rbp
 push r12
 mov rbp, rsp
 # Stack pointer unchanged, no stack allocated variables
 # push {rsi, rdi}
 sub rsp, 16
 mov qword ptr [rsp], rsi
 mov qword ptr [rsp + 8], rdi
 # Set up R11 as a temporary second base pointer for the caller saved things
 mov r11, rsp
 # Stack pointer unchanged, no stack allocated arguments
 mov eax, edi
 add eax, esi
 jo _errOverflow
 movsx rax, eax
 push rax
 pop rax
 mov rax, rax
 mov rdi, rax
 mov rax, qword ptr [r11 + 8]
 mov eax, eax
 sub eax, esi
 jo _errOverflow
 movsx rax, eax
 push rax
 pop rax
 mov rax, rax
 mov rsi, rax
 mov rbx, qword ptr [r11]
 mov rax, qword ptr [r11 + 8]
 mov eax, eax
 imul eax, ebx
 jo _errOverflow
 movsx rax, eax
 push rax
 pop rax
 mov rax, rax
 mov rdx, rax
 call wacc_g
 mov r11, rax
 # Stack pointer unchanged, no stack allocated arguments
 # pop {rsi, rdi}
 mov rsi, qword ptr [rsp]
 mov rdi, qword ptr [rsp + 8]
 add rsp, 16
 mov rax, r11
 mov r12, rax
 mov rax, r12
 # reset the stack pointer, undoing any pushes: this is often unnecessary, but is cheap
 mov rsp, rbp
 pop r12
 pop rbp
 ret
 # Stack pointer unchanged, no stack allocated variables
 # 'ere be dragons: this is 100% dead code, functions always end in returns!

wacc_g:
 push rbp
 mov rbp, rsp
 # push {rdx, rsi, rdi}
 sub rsp, 24
 mov qword ptr [rsp], rdx
 mov qword ptr [rsp + 8], rsi
 mov qword ptr [rsp + 16], rdi
 # Set up R11 as a temporary second base pointer for the caller saved things
 mov r11, rsp
 # Stack pointer unchanged, no stack allocated arguments
 mov rax, rdi
 mov rdi, rax
 # statement primitives do not return results (but will clobber r0/rax)
 call _printi
 call _println
 # pop {rdx, rsi, rdi}
 mov rdx, qword ptr [rsp]
 mov rsi, qword ptr [rsp + 8]
 mov rdi, qword ptr [rsp + 16]
 add rsp, 24
 # push {rdx, rsi, rdi}
 sub rsp, 24
 mov qword ptr [rsp], rdx
 mov qword ptr [rsp + 8], rsi
 mov qword ptr [rsp + 16], rdi
 # Set up R11 as a temporary second base pointer for the caller saved things
 mov r11, rsp
 # Stack pointer unchanged, no stack allocated arguments
 mov rax, rsi
 mov rdi, rax
 # statement primitives do not return results (but will clobber r0/rax)
 call _printi
 call _println
 # pop {rdx, rsi, rdi}
 mov rdx, qword ptr [rsp]
 mov rsi, qword ptr [rsp + 8]
 mov rdi, qword ptr [rsp + 16]
 add rsp, 24
 # push {rdx, rsi, rdi}
 sub rsp, 24
 mov qword ptr [rsp], rdx
 mov qword ptr [rsp + 8], rsi
 mov qword ptr [rsp + 16], rdi
 # Set up R11 as a temporary second base pointer for the caller saved things
 mov r11, rsp
 # Stack pointer unchanged, no stack allocated arguments
 mov rax, rdx
 mov rdi, rax
 # statement primitives do not return results (but will clobber r0/rax)
 call _printi
 call _println
 # pop {rdx, rsi, rdi}
 mov rdx, qword ptr [rsp]
 mov rsi, qword ptr [rsp + 8]
 mov rdi, qword ptr [rsp + 16]
 add rsp, 24
 mov rax, 0
 # reset the stack pointer, undoing any pushes: this is often unnecessary, but is cheap
 mov rsp, rbp
 pop rbp
 ret
 # 'ere be dragons: this is 100% dead code, functions always end in returns!

.section .rodata
# length of .L._prints_str0
 .int 4
.L._prints_str0:
 .asciz "%.*s"
.text
_prints:
 push rbp
 mov rbp, rsp
 # external calls must be stack-aligned to 16 bytes, accomplished by masking with fffffffffffffff0
 and rsp, -16
 mov rdx, rdi
 mov esi, dword ptr [rdi - 4]
 lea rdi, [rip + .L._prints_str0]
 # on x86, al represents the number of SIMD registers used as variadic arguments
 mov al, 0
 call printf@plt
 mov rdi, 0
 call fflush@plt
 mov rsp, rbp
 pop rbp
 ret

.section .rodata
# length of .L._printi_str0
 .int 2
.L._printi_str0:
 .asciz "%d"
.text
_printi:
 push rbp
 mov rbp, rsp
 # external calls must be stack-aligned to 16 bytes, accomplished by masking with fffffffffffffff0
 and rsp, -16
 mov esi, edi
 lea rdi, [rip + .L._printi_str0]
 # on x86, al represents the number of SIMD registers used as variadic arguments
 mov al, 0
 call printf@plt
 mov rdi, 0
 call fflush@plt
 mov rsp, rbp
 pop rbp
 ret

.section .rodata
# length of .L._println_str0
 .int 0
.L._println_str0:
 .asciz ""
.text
_println:
 push rbp
 mov rbp, rsp
 # external calls must be stack-aligned to 16 bytes, accomplished by masking with fffffffffffffff0
 and rsp, -16
 lea rdi, [rip + .L._println_str0]
 call puts@plt
 mov rdi, 0
 call fflush@plt
 mov rsp, rbp
 pop rbp
 ret

.section .rodata
# length of .L._errOverflow_str0
 .int 52
.L._errOverflow_str0:
 .asciz "fatal error: integer overflow or underflow occurred\n"
.text
_errOverflow:
 # external calls must be stack-aligned to 16 bytes, accomplished by masking with fffffffffffffff0
 and rsp, -16
 lea rdi, [rip + .L._errOverflow_str0]
 call _prints
 mov dil, -1
 call exit@plt
