.intel_syntax noprefix
.globl main
.section .rodata
# length of .L.str0
	.int 5
.L.str0:
	.asciz "Wrong"
# length of .L.str1
	.int 7
.L.str1:
	.asciz "Correct"
.text
main:
	push rbp
	# push {rbx, r12}
	sub rsp, 16
	mov qword ptr [rsp], rbx
	mov qword ptr [rsp + 8], r12
	mov rbp, rsp
	# Stack pointer unchanged, no stack allocated variables
	mov rax, 1
	cmp rax, 1
	jne .L0
	mov rax, 0
	cmp rax, 1
.L0:
	sete al
	movsx rax, al
	push rax
	pop rax
	cmp rax, 1
	je .L1
	mov rax, 1
	cmp rax, 1
	jne .L2
	mov rax, 0
	cmp rax, 1
.L2:
	sete al
	movsx rax, al
	push rax
	pop rax
	cmp rax, 1
.L1:
	setne al
	movsx rax, al
	push rax
	pop rax
	mov rax, rax
	mov r12, rax
	cmp r12, 1
	je .L3
	# Stack pointer unchanged, no stack allocated arguments
	lea rax, [rip + .L.str0]
	push rax
	pop rax
	mov rax, rax
	mov rdi, rax
	# statement primitives do not return results (but will clobber r0/rax)
	call _prints
	call _println
	jmp .L4
.L3:
	# Stack pointer unchanged, no stack allocated arguments
	lea rax, [rip + .L.str1]
	push rax
	pop rax
	mov rax, rax
	mov rdi, rax
	# statement primitives do not return results (but will clobber r0/rax)
	call _prints
	call _println
.L4:
	# Stack pointer unchanged, no stack allocated variables
	mov rax, 0
	# pop {rbx, r12}
	mov rbx, qword ptr [rsp]
	mov r12, qword ptr [rsp + 8]
	add rsp, 16
	pop rbp
	ret

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
