.intel_syntax noprefix
.globl main
.section .rodata
.text
main:
 push rbp
 # push {rbx, r12, r13, r14, r15}
 sub rsp, 40
 mov qword ptr [rsp], rbx
 mov qword ptr [rsp + 8], r12
 mov qword ptr [rsp + 16], r13
 mov qword ptr [rsp + 24], r14
 mov qword ptr [rsp + 32], r15
 mov rbp, rsp
 # Stack pointer unchanged, no stack allocated variables
 mov rax, 2
 mov r12, rax
 mov rax, 4
 mov r13, rax
 mov rax, 4
 mov r14, rax
 cmp r12, r13
 sete al
 movsx rax, al
 push rax
 pop rax
 mov rax, rax
 mov r15, rax
 # Stack pointer unchanged, no stack allocated arguments
 mov rax, r15
 mov rdi, rax
 # statement primitives do not return results (but will clobber r0/rax)
 call _printb
 call _println
 # Stack pointer unchanged, no stack allocated arguments
 cmp r12, r13
 sete al
 movsx rax, al
 push rax
 pop rax
 mov rax, rax
 mov rdi, rax
 # statement primitives do not return results (but will clobber r0/rax)
 call _printb
 call _println
 # Stack pointer unchanged, no stack allocated arguments
 cmp r13, r14
 sete al
 movsx rax, al
 push rax
 pop rax
 mov rax, rax
 mov rdi, rax
 # statement primitives do not return results (but will clobber r0/rax)
 call _printb
 call _println
 # Stack pointer unchanged, no stack allocated variables
 mov rax, 0
 # pop {rbx, r12, r13, r14, r15}
 mov rbx, qword ptr [rsp]
 mov r12, qword ptr [rsp + 8]
 mov r13, qword ptr [rsp + 16]
 mov r14, qword ptr [rsp + 24]
 mov r15, qword ptr [rsp + 32]
 add rsp, 40
 pop rbp
 ret

.section .rodata
# length of .L._printb_str0
 .int 5
.L._printb_str0:
 .asciz "false"
# length of .L._printb_str1
 .int 4
.L._printb_str1:
 .asciz "true"
# length of .L._printb_str2
 .int 4
.L._printb_str2:
 .asciz "%.*s"
.text
_printb:
 push rbp
 mov rbp, rsp
 # external calls must be stack-aligned to 16 bytes, accomplished by masking with fffffffffffffff0
 and rsp, -16
 cmp dil, 0
 jne .L_printb0
 lea rdx, [rip + .L._printb_str0]
 jmp .L_printb1
.L_printb0:
 lea rdx, [rip + .L._printb_str1]
.L_printb1:
 mov esi, dword ptr [rdx - 4]
 lea rdi, [rip + .L._printb_str2]
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
