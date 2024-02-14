import socket
import threading
import json
import time

# Aggiungi un lock globale
lock = threading.Lock()

# Dichiarazione globale della variabile completed
completed = False

# Aggiungi la variabile globale per tenere traccia dell'ultimo valore di "end" salvato
last_saved_end = 0

def handle_client(client_socket, address, available_clients):
    data = client_socket.recv(1024).decode()
    try:
        json_data = json.loads(data)
        if "disponibile" in json_data:
            with lock:
                print(f"Client {address} is available.")
                available_clients.append(client_socket)
    except json.JSONDecodeError:
        pass

def check_range_completion(request_start, request_end):
    with open("ranges.txt", "r") as file:
        lines = file.readlines()
        for line in lines:
            s = line.strip().split('-')
            if len(s) >= 2:
                stored_start = int(s[0])
                stored_end = int(s[1])
            else:
                stored_start = 0
                stored_end = 0
            if request_start == stored_start and request_end == stored_end:
                return True
    return False

def distribute_work(available_clients, end_range, request_start, request_end):
    global completed, last_saved_end, start_range  # Dichiarazione della variabile completed e last_saved_end come globali

    step = 100

    while check_range_completion(start_range, start_range + step):
        print(f"Range {start_range}-{start_range + step} already completed. Skipping calculation.")
        completed = True
        start_range += step

    if (start_range + step > end_range + 1):
        completed = True

    for client_socket in available_clients:
        if (completed == True  and start_range + step > end_range + 1):
            completed = False
            range_data = {"completed": True}
            data_json = json.dumps(range_data) + "\n"
            client_socket.send(data_json.encode())
        else:
            if(completed == True):
                completed = False

            print(f"Work distribution for range {request_start}-{request_end}")
            range_data = {"start": start_range, "end": start_range + step}
            data_json = json.dumps(range_data) + "\n"
            client_socket.send(data_json.encode())
            start_range += step

            if start_range > last_saved_end:
                save_range_to_file(start_range - step, start_range, request_start, request_end)
                last_saved_end = start_range

def save_range_to_file(start, end, request_start, request_end):
    with open("ranges.txt", "a") as file:
        file.write(f"{start}-{end}\n")

def save_prime_numbers_to_file(prime_numbers):
    with open("primeNumbers.txt", "a") as file:
        file.write(f"{prime_numbers}\n")

def receive_and_print_primes(available_clients):
    for client_socket in available_clients:
        result_data = client_socket.recv(1024).decode()
        result_data = json.loads(result_data)

        start_range = result_data.get("start", None)
        end_range = result_data.get("end", None)
        prime_numbers = result_data.get("prime_numbers", [])
        completed = result_data.get("completed", False)

        if start_range is not None and end_range is not None and start_range != 0 and end_range != 0:
            print(f"Prime numbers received from {client_socket.getpeername()} for range {start_range}-{end_range}: {prime_numbers}")
            save_prime_numbers_to_file(prime_numbers)

            if completed:
                print(f"All prime numbers in the range {start_range}-{end_range} have been calculated by {client_socket.getpeername()}.")
                if start_range > last_saved_end:
                    save_range_to_file(start_range, end_range, start_range, end_range)
        else:
            if start_range == 0 and end_range == 0:
                print("calculation already completed")
            else:
                print(f"Invalid data received from {client_socket.getpeername()}")

def main():
    global completed, last_saved_end, start_range  # Dichiarazione della variabile completed e last_saved_end come globali

    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind(('0.0.0.0', 5555))
    server.listen(5)

    print("Server listening on port 5555")

    available_clients = []
    start_range = 1
    end_range = 1000000000

    while True:
        client, addr = server.accept()
        client_handler = threading.Thread(target=handle_client, args=(client, addr, available_clients))
        client_handler.start()

        for thread in threading.enumerate():
            if thread != threading.current_thread():
                thread.join()

        if len(available_clients) <= 3 and len(available_clients) > 0:
            distribute_work(available_clients, end_range, start_range, end_range)
            time.sleep(1)

            if not completed:
                receive_and_print_primes(available_clients)
            else:
                print("Calculation completed")

            available_clients = []

if __name__ == "__main__":
    main()
