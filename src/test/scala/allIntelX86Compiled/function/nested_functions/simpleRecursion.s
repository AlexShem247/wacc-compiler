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
	mov rax, 0
	mov r12, rax
	# Stack pointer unchanged, no stack allocated arguments
	mov rax, 8
	mov rdi, rax
	call wacc_rec
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

wacc_rec:
	push rbp
	push r12
	mov rbp, rsp
	cmp rdi, 0
	je .L0
	# Stack pointer unchanged, no stack allocated variables
	push rdi
	# Set up R11 as a temporary second base pointer for the caller saved things
	mov r11, rsp
	# Stack pointer unchanged, no stack allocated arguments
	mov eax, edi
	sub eax, 1
	jo _errOverflow
	movsx rax, eax
	push rax
	pop rax
	mov rax, rax
	mov rdi, rax
	call wacc_rec
	mov r11, rax
	# Stack pointer unchanged, no stack allocated arguments
	pop rdi
	mov rax, r11
	mov r12, rax
	# Stack pointer unchanged, no stack allocated variables
	jmp .L1
.L0:
.L1:
	mov rax, 42
	# reset the stack pointer, undoing any pushes: this is often unnecessary, but is cheap
	mov rsp, rbp
	pop r12
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
