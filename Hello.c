#include <c++/v1/cstdio>
#include <c++/v1/__new/global_new_delete.h>
#include <c++/v1/__new/placement_new_delete.h>

int main(int argc, const char* argv[]) {
    printf("Hello, World!\n");

    Node* list = NULL;
    insert(&list, NULL, "four");
    insert(&list, NULL, "one");
    insert(&list, find(list, "one"), "two");
    insert(&list, find(list, "two"), "three");

    dump(list);
    printf("-- delete three --\n");
    delete(&list, find(list, "three"));
    dump(list);

    printf("-- delete one --\n");
    delete(&list, find(list, "one"));
    dump(list);

    return 0;
}
